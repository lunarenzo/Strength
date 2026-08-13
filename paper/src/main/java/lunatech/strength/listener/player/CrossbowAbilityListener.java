package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.CrossbowImmobilizeTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener managing Crossbow abilities: 3rd shot passive damage multipliers,
 * back-facing slowness potion triggers, tranquilizer ultimate shot immobilization,
 * location locking, and teleport cancellations.
 */
public final class CrossbowAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Collections to manage active weapon state and eliminate memory leaks
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> crossbowUltimatePrimed = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> immobilizedPlayers = new ConcurrentHashMap<>();

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

        projectile.setMetadata("CrossbowArrow", new FixedMetadataValue(plugin, true));

        if (crossbowUltimatePrimed.getOrDefault(uuid, false)) {
            crossbowUltimatePrimed.put(uuid, false);
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

        if (!arrow.hasMetadata("CrossbowArrow")) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"crossbow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID shooterUuid = shooter.getUniqueId();
        final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();

        // 1. Back-Facing Slowness Check
        final Vector sLook = shooter.getLocation().getDirection().setY(0).normalize();
        final Vector vLook = victim.getLocation().getDirection().setY(0).normalize();
        if (sLook.dot(vLook) > 0.0) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, true));
            shooter.sendMessage(ColorParser.of(settings.slownessAppliedMessage).build());
        }

        // 2. Passive Hit Tracker: Every 3rd shot hit deal 2x damage
        final int currentPassiveHits = passiveHits.merge(shooterUuid, 1, Integer::sum);
        if (currentPassiveHits >= 3) {
            passiveHits.put(shooterUuid, 0); // reset count

            event.setDamage(event.getDamage() * 2.0);
            shooter.sendMessage(ColorParser.of(settings.passiveTriggeredShooterMessage).build());

            // 3. Ultimate Charge Increment
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

        // 4. Tranquilizer Ultimate Shot Trigger
        if (arrow.hasMetadata("CrossbowUltimateArrow")) {
            final UUID victimUuid = victim.getUniqueId();
            final Location freezeLoc = victim.getLocation().clone();

            immobilizedPlayers.put(victimUuid, freezeLoc);
            new CrossbowImmobilizeTask(victim, settings.immobilizeDurationSeconds)
                .runTaskTimer(plugin, 0L, 1L);

            victim.getWorld().playSound(victim.getLocation(), Sound.ITEM_CROSSBOW_HIT, 1.0f, 0.5f);
            victim.sendMessage(ColorParser.of(settings.immobilizedVictimMessage).build());
            shooter.sendMessage(ColorParser.of(settings.immobilizedShooterMessage).build());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(@NotNull PlayerMoveEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        final Location freezeLoc = immobilizedPlayers.get(uuid);

        if (freezeLoc != null) {
            final Location from = event.getFrom();
            final Location to = event.getTo();

            // Block X, Y, Z translation while preserving camera pitch & yaw rotation
            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                final Location target = freezeLoc.clone();
                target.setPitch(to.getPitch());
                target.setYaw(to.getYaw());
                event.setTo(target);
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
    }
}
