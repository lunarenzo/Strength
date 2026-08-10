package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.BowConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Bow abilities: tracking projectile hits, spawning passive llama spit trails,
 * trapping targets in temporary cobwebs, and managing ultimate charging.
 */
public final class BowAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Concurrent maps for tracking states. Cleaned up immediately on quit to ensure zero heap retention.
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> bowPassiveReady = new ConcurrentHashMap<>();

    public BowAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBow(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter) || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        // Verify shooter has Bow weapon role assigned
        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"bow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID uuid = shooter.getUniqueId();
        if (bowPassiveReady.getOrDefault(uuid, false)) {
            bowPassiveReady.put(uuid, false); // Consume passive

            // Tag arrow as passive
            arrow.setMetadata("BowPassiveArrow", new FixedMetadataValue(plugin, true));

            // Start llama spit particle trail task
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (arrow.isDead() || arrow.isOnGround()) {
                        cancel();
                        return;
                    }
                    arrow.getWorld().spawnParticle(Particle.SPIT, arrow.getLocation(), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }.runTaskTimer(plugin, 0L, 1L);

            shooter.sendMessage(ColorParser.of(plugin.getConfigHandler().getBowConfig().passiveTriggeredShooterMessage).build());
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_LLAMA_SPIT, 1.0f, 1.0f);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // Verify damage was caused by an Arrow shot by a Player
        if (!(event.getDamager() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        // Verify shooter has Bow weapon role assigned
        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"bow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID shooterUuid = shooter.getUniqueId();
        final BowConfig settings = plugin.getConfigHandler().getBowConfig();

        // 1. Passive hit tracking: Increment hits
        final int currentPassiveHits = passiveHits.merge(shooterUuid, 1, Integer::sum);
        if (currentPassiveHits >= settings.passiveHitsRequired) {
            passiveHits.put(shooterUuid, 0); // Reset
            bowPassiveReady.put(shooterUuid, true); // Next arrow is passive

            shooter.sendMessage(ColorParser.of(settings.passiveReadyShooterMessage).build());
            shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        // 2. Ultimate hit tracking: Increment hits (only counts if hit other player and did damage)
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

        // 3. Passive Cobweb Trap Trigger
        if (arrow.hasMetadata("BowPassiveArrow")) {
            arrow.removeMetadata("BowPassiveArrow", plugin);

            final Block block = victim.getLocation().getBlock();
            if (block.getType() == Material.AIR || block.getType().isAir()) {
                final BlockData originalData = block.getBlockData();
                block.setType(Material.COBWEB);

                victim.sendMessage(ColorParser.of(settings.passiveTrappedVictimMessage).build());
                victim.playSound(victim.getLocation(), Sound.ENTITY_SPIDER_DEATH, 1.0f, 0.8f);

                // Schedule automatic cobweb removal/restoration
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (block.getType() == Material.COBWEB) {
                        block.setBlockData(originalData);
                    }
                }, settings.passiveCobwebDurationSeconds * 20L);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
        bowPassiveReady.remove(uuid);
    }
}
