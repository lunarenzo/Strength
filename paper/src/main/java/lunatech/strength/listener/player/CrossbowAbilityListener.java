package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.CrossbowImmobilizeTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener managing Crossbow abilities: Nth shot passive damage multipliers,
 * persistent data projectile tracking, multishot cooldown protection, tranquilizer ultimate shot immobilization,
 * rotation & position freeze locking, and dismount/teleport cancellations.
 */
public final class CrossbowAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final NamespacedKey CROSSBOW_ARROW_KEY = new NamespacedKey("strength", "crossbow_arrow");
    public static final NamespacedKey CROSSBOW_ULT_KEY = new NamespacedKey("strength", "crossbow_ult_arrow");

    // Collections to manage active weapon state and eliminate memory leaks
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> crossbowUltimatePrimed = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> immobilizedPlayers = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastPassiveHitTime = new ConcurrentHashMap<>();

    public CrossbowAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrossbowShoot(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getBow() == null || event.getBow().getType() != Material.CROSSBOW) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(player);
        if (!"crossbow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        final Projectile projectile = (Projectile) event.getProjectile();

        // Edge Case 1: Attach PersistentDataContainer tags to projectile at shoot time to prevent mid-air weapon swap exploits
        projectile.getPersistentDataContainer().set(CROSSBOW_ARROW_KEY, PersistentDataType.BYTE, (byte) 1);
        projectile.setMetadata("CrossbowArrow", new FixedMetadataValue(plugin, true));

        if (crossbowUltimatePrimed.getOrDefault(uuid, false)) {
            crossbowUltimatePrimed.put(uuid, false);
            projectile.getPersistentDataContainer().set(CROSSBOW_ULT_KEY, PersistentDataType.BYTE, (byte) 1);
            projectile.setMetadata("CrossbowUltimateArrow", new FixedMetadataValue(plugin, true));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowHitEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        if (!(event.getDamager() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        // Edge Case 1 Verification: Validate PersistentDataContainer or metadata tag attached during launch
        final boolean isCrossbowArrow = arrow.getPersistentDataContainer().has(CROSSBOW_ARROW_KEY, PersistentDataType.BYTE)
            || arrow.hasMetadata("CrossbowArrow");
        if (!isCrossbowArrow) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"crossbow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID shooterUuid = shooter.getUniqueId();
        final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();

        // Edge Case 2: Multishot Cooldown Check (5 ticks / 250ms) to prevent single Multishot burst from multi-incrementing passive
        final long now = System.currentTimeMillis();
        final long lastHit = lastPassiveHitTime.getOrDefault(shooterUuid, 0L);
        if (now - lastHit >= 250L) {
            lastPassiveHitTime.put(shooterUuid, now);

            // 1. Passive Hit Tracker: Every Nth shot hit deals configurable damage multiplier
            final int currentPassiveHits = passiveHits.merge(shooterUuid, 1, Integer::sum);
            if (currentPassiveHits >= settings.passiveHitsRequired) {
                passiveHits.put(shooterUuid, 0); // reset count

                event.setDamage(event.getDamage() * settings.passiveDamageMultiplier);
                shooter.sendMessage(ColorParser.of(settings.passiveTriggeredShooterMessage).build());

                // 2. Ultimate Charge Increment
                final int currentUltHits = ultimateHits.getOrDefault(shooterUuid, 0);
                final int targetUltHits = settings.ultimateHitsRequired;
                if (currentUltHits < targetUltHits) {
                    final int nextUltHits = currentUltHits + 1;
                    ultimateHits.put(shooterUuid, nextUltHits);

                    if (nextUltHits == targetUltHits) {
                        shooter.sendMessage(ColorParser.of(settings.ultimateChargedMessage).build());
                        shooter.playSound(shooter.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                    } else {
                        shooter.sendMessage(
                            ColorParser.of(settings.ultimateChargeProgressMessage)
                                .with("charge", String.valueOf(nextUltHits))
                                .with("target", String.valueOf(targetUltHits))
                                .build()
                        );
                    }
                }
            }
        }

        // 3. Crossbow Ultimate Shot Trigger
        final boolean isUltArrow = arrow.getPersistentDataContainer().has(CROSSBOW_ULT_KEY, PersistentDataType.BYTE)
            || arrow.hasMetadata("CrossbowUltimateArrow");
        if (isUltArrow) {
            final UUID victimUuid = victim.getUniqueId();
            final Location freezeLoc = victim.getLocation().clone();

            // Spawn invisible ArmorStand vehicle to natively disable WASD vehicle movement
            final org.bukkit.entity.ArmorStand vehicle = victim.getWorld().spawn(freezeLoc, org.bukkit.entity.ArmorStand.class, as -> {
                as.setVisible(false);
                as.setMarker(true);
                as.setGravity(false);
                as.setSmall(true);
                as.setPersistent(false);
            });
            vehicle.addPassenger(victim);

            victim.setVelocity(new Vector(0, 0, 0));
            immobilizedPlayers.put(victimUuid, freezeLoc);

            // Nullify next-tick knockback velocity applied by Minecraft server engine
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> {
                if (victim.isOnline()) {
                    victim.setVelocity(new Vector(0, 0, 0));
                    immobilizedPlayers.put(victimUuid, victim.getLocation().clone());
                }
            });

            new CrossbowImmobilizeTask(victim, vehicle, settings.immobilizeDurationSeconds)
                .runTaskTimer(plugin, 0L, 1L);

            victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_CROSSBOW_HIT, 1.0f, 0.5f);
            victim.sendMessage(ColorParser.of(settings.immobilizedVictimMessage).build());
            shooter.sendMessage(ColorParser.of(settings.immobilizedShooterMessage).build());
        }
    }

    // Edge Case 3: Lock BOTH position (X, Y, Z) and screen rotation (Yaw, Pitch) in place so immobilized player cannot turn POV
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final Location freezeLoc = immobilizedPlayers.get(uuid);

        if (freezeLoc != null) {
            event.setTo(freezeLoc.clone());
        }
    }

    // Edge Case 4: Prevent Shift-Click dismounting out of ultimate freeze vehicle
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDismount(@NotNull EntityDismountEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (immobilizedPlayers.containsKey(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();

        if (immobilizedPlayers.containsKey(uuid)) {
            event.setCancelled(true);
            final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();
            event.getPlayer().sendMessage(ColorParser.of(settings.trapEscapeBlockedMessage).build());
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
        crossbowUltimatePrimed.remove(uuid);
        immobilizedPlayers.remove(uuid);
        lastPassiveHitTime.remove(uuid);
    }
}
