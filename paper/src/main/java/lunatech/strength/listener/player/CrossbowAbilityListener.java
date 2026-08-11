package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.CrossbowTrapTask;
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
import org.bukkit.event.entity.ProjectileHitEvent;
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
 * Listener that manages Crossbow abilities: tracking 3rd shot passive damage multipliers,
 * back-facing slowness potion triggers, ultimate arrow priming, anchoring block traps,
 * and teleport caging handlers.
 */
public final class CrossbowAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Collections to manage active weapon state and eliminate memory leaks
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> crossbowUltimatePrimed = new ConcurrentHashMap<>();
    public static final Map<UUID, Location> trappedPlayers = new ConcurrentHashMap<>();

    public CrossbowAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrossbowShoot(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Verify player is holding a crossbow
        if (event.getBow() == null || event.getBow().getType() != Material.CROSSBOW) {
            return;
        }

        // Verify player has Crossbow weapon assigned
        final String assigned = strengthService.getAssignedWeapon(player);
        if (!"crossbow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        final Projectile projectile = (Projectile) event.getProjectile();

        // Tag projectile as crossbow arrow
        projectile.setMetadata("CrossbowArrow", new FixedMetadataValue(plugin, true));

        // If ultimate is primed, consume and tag the projectile as ultimate arrow
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

        // Verify damage was caused by an arrow shot by a player
        if (!(event.getDamager() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        // Verify arrow was shot from a Crossbow
        if (!arrow.hasMetadata("CrossbowArrow")) {
            return;
        }

        // Verify shooter has Crossbow assigned
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
            // Inflict slowness 1 natively (60 ticks = 3 seconds)
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 0, true, true));
            shooter.sendMessage(ColorParser.of(settings.slownessAppliedMessage).build());
        }

        // 2. Passive Hit Tracker: Every 3rd shot hit deal 2x damage
        final int currentPassiveHits = passiveHits.merge(shooterUuid, 1, Integer::sum);
        if (currentPassiveHits >= 3) {
            passiveHits.put(shooterUuid, 0); // reset count
            
            // Set 2x damage
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
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProjectileHit(@NotNull ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        // Verify arrow is an ultimate arrow
        if (!arrow.hasMetadata("CrossbowUltimateArrow")) {
            return;
        }

        final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();

        // If arrow hit a player/entity directly, it fizzles
        if (event.getHitEntity() != null) {
            shooter.sendMessage(ColorParser.of(settings.ultimateFlippedMessage).build());
            return;
        }

        // If arrow hit a block, spawn the anchor
        if (event.getHitBlock() != null) {
            // Anchor location centered on the hit block surface
            final Location anchorLoc = event.getHitBlock().getLocation().add(0.5, 1.0, 0.5);
            
            // Remove the arrow entity
            arrow.remove();

            // Sound feedback
            anchorLoc.getWorld().playSound(anchorLoc, Sound.BLOCK_CHAIN_PLACE, 1.0f, 1.0f);

            // Start repeating caging check task
            new CrossbowTrapTask(shooter, anchorLoc, settings)
                .runTaskTimer(plugin, 0L, 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerTeleport(@NotNull PlayerTeleportEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        
        // If victim is trapped, block teleport attempts from pearl/chorus/command/plugin
        if (trappedPlayers.containsKey(uuid)) {
            final PlayerTeleportEvent.TeleportCause cause = event.getCause();
            if (cause == PlayerTeleportEvent.TeleportCause.ENDER_PEARL ||
                cause == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT ||
                cause == PlayerTeleportEvent.TeleportCause.COMMAND ||
                cause == PlayerTeleportEvent.TeleportCause.PLUGIN) {
                
                event.setCancelled(true);
                
                final Player player = event.getPlayer();
                final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();
                player.sendMessage(ColorParser.of(settings.trapEscapeBlockedMessage).build());
                player.playSound(player.getLocation(), Sound.BLOCK_CHAIN_HIT, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
        crossbowUltimatePrimed.remove(uuid);
        trappedPlayers.remove(uuid);
    }
}
