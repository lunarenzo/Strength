package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.BowConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Bow abilities: tracking projectile hits, spawning passive llama spit trails,
 * trapping targets in temporary cobwebs, and managing ultimate charging and cooldowns.
 */
public final class BowAbilityListener implements Listener {
    private static final NamespacedKey BOW_ARROW_KEY = new NamespacedKey("strength", "bow_arrow");
    private static final NamespacedKey BOW_PASSIVE_KEY = new NamespacedKey("strength", "bow_passive_arrow");

    private final Strength plugin;
    private final StrengthService strengthService;

    // Concurrent maps for tracking states. Cleaned up immediately on quit to ensure zero heap retention.
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> bowPassiveReady = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();

    // Edge Case 3: Active temporary cobwebs and their original block data for server shutdown restoration
    public static final Map<Location, BlockData> activeCobwebs = new ConcurrentHashMap<>();

    public BowAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityShootBow(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter) || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        // Verify the item used is a standard Bow (not a Crossbow)
        if (event.getBow() == null || event.getBow().getType() != Material.BOW) {
            return;
        }

        // Edge Case 1: Verify shooter has Bow weapon role assigned
        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"bow".equalsIgnoreCase(assigned)) {
            return;
        }

        // Tag projectile with persistent data to prevent weapon swap exploits
        arrow.getPersistentDataContainer().set(BOW_ARROW_KEY, PersistentDataType.BYTE, (byte) 1);

        final UUID uuid = shooter.getUniqueId();
        if (bowPassiveReady.getOrDefault(uuid, false)) {
            bowPassiveReady.put(uuid, false); // Consume passive trigger

            // Tag arrow as passive cobweb arrow
            arrow.getPersistentDataContainer().set(BOW_PASSIVE_KEY, PersistentDataType.BYTE, (byte) 1);

            // Start particle trail task
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

        // Edge Case 1: Verify projectile has Bow PDC key
        final boolean isBowArrow = arrow.getPersistentDataContainer().has(BOW_ARROW_KEY, PersistentDataType.BYTE)
            || arrow.hasMetadata("BowPassiveArrow");
        if (!isBowArrow) {
            return;
        }

        // Verify shooter has Bow weapon role assigned
        final String assigned = strengthService.getAssignedWeapon(shooter);
        if (!"bow".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID shooterUuid = shooter.getUniqueId();
        final BowConfig settings = plugin.getConfigHandler().getBowConfig();

        final boolean isPassiveArrow = arrow.getPersistentDataContainer().has(BOW_PASSIVE_KEY, PersistentDataType.BYTE)
            || arrow.hasMetadata("BowPassiveArrow");

        if (isPassiveArrow) {
            // Nullify knockback velocity (including Punch I/II or full-charge bow velocity) so target doesn't fly out of trap
            victim.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

            // Edge Case 2: 1-tick delay to anchor target directly inside post-hit chest cobweb
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!victim.isOnline()) return;

                victim.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

                // Select target's updated post-knockback chest block
                final Location victimLoc = victim.getLocation();
                Block chestBlock = victimLoc.getBlock().getRelative(BlockFace.UP);
                if (!isBlockSafeToReplace(chestBlock)) {
                    chestBlock = victimLoc.getBlock();
                }

                // Edge Case 1: Only place cobweb if block is safe and not a tile entity/container/protected block
                if (isBlockSafeToReplace(chestBlock)) {
                    final Location blockLoc = chestBlock.getLocation();
                    final BlockData originalData = chestBlock.getBlockData();

                    activeCobwebs.put(blockLoc, originalData);
                    chestBlock.setType(Material.COBWEB);

                    victim.sendMessage(ColorParser.of(settings.passiveTrappedVictimMessage).build());
                    victim.playSound(victimLoc, Sound.ENTITY_SPIDER_DEATH, 1.0f, 0.8f);

                    // Schedule automatic cobweb removal and block state restoration
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        restoreCobwebBlock(blockLoc);
                    }, settings.passiveCobwebDurationSeconds * 20L);
                }
            }, 1L);
        } else {
            // 1. Passive hit tracking: Increment hits (ONLY for normal non-passive bow hits)
            final int currentPassiveHits = passiveHits.merge(shooterUuid, 1, Integer::sum);
            if (currentPassiveHits >= settings.passiveHitsRequired) {
                passiveHits.put(shooterUuid, 0); // Reset count
                bowPassiveReady.put(shooterUuid, true); // Next valid shot will trap the target in a cobweb

                shooter.sendMessage(ColorParser.of(settings.passiveReadyShooterMessage).build());
                shooter.playSound(shooter.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }

            // 2. Ultimate hit tracking: Increment hits
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
                        ColorParser.of(settings.ultimateChargeProgressMessage
                            .replace("{charge}", String.valueOf(nextUltHits))
                            .replace("{target}", String.valueOf(targetUltHits)))
                            .with("charge", String.valueOf(nextUltHits))
                            .with("target", String.valueOf(targetUltHits))
                            .build()
                    );
                }
            }
        }
    }

    // Edge Case 4: If a player breaks the temporary cobweb trap, cancel item drops and revert block cleanly
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCobwebBreak(@NotNull BlockBreakEvent event) {
        final Location loc = event.getBlock().getLocation();
        if (activeCobwebs.containsKey(loc)) {
            event.setDropItems(false);
            restoreCobwebBlock(loc);
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
        bowPassiveReady.remove(uuid);
        ultimateCooldowns.remove(uuid);
    }

    // Edge Case 1 Safety Check: Prevent deleting tile entities (chests, shulker boxes, furnaces) or non-replaceable blocks
    private boolean isBlockSafeToReplace(@NotNull Block block) {
        final Material type = block.getType();
        if (type == Material.COBWEB) return false;
        if (block.getState() instanceof org.bukkit.block.TileState) return false;
        if (block.getState() instanceof org.bukkit.block.Container) return false;
        return block.isReplaceable() || type == Material.AIR || type.isAir();
    }

    private static void restoreCobwebBlock(@NotNull Location location) {
        final BlockData originalData = activeCobwebs.remove(location);
        if (originalData != null && location.getBlock().getType() == Material.COBWEB) {
            location.getBlock().setBlockData(originalData);
        }
    }

    // Edge Case 3: Revert all active cobwebs instantly on plugin disable / server shutdown
    public static void cleanupActiveCobwebs() {
        for (Map.Entry<Location, BlockData> entry : activeCobwebs.entrySet()) {
            final Location loc = entry.getKey();
            final BlockData data = entry.getValue();
            if (loc != null && loc.getWorld() != null && loc.getBlock().getType() == Material.COBWEB) {
                loc.getBlock().setBlockData(data);
            }
        }
        activeCobwebs.clear();
    }
}
