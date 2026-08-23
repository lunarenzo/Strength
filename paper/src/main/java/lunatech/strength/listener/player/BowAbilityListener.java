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
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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
    public static final Map<UUID, Integer> remainingUltShots = new ConcurrentHashMap<>();
    public static final Map<UUID, ItemDisplay> activeAimSpirals = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> chargeSoundPlayed = new ConcurrentHashMap<>();

    // Edge Case 3: Active temporary cobwebs and their original block data for server shutdown restoration
    public static final Map<Location, BlockData> activeCobwebs = new ConcurrentHashMap<>();

    public BowAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;

        // Real-time Aiming Laser Guide & Face Spiral Charge Task
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (remainingUltShots.isEmpty()) return;

            for (Map.Entry<UUID, Integer> entry : remainingUltShots.entrySet()) {
                if (entry.getValue() <= 0) continue;
                final Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null || !player.isOnline()) continue;

                final UUID uuid = player.getUniqueId();
                final boolean isDrawing = player.isHandRaised() && player.getInventory().getItemInMainHand().getType() == Material.BOW;
                final boolean isFullDraw = isDrawing && player.getActiveItemUsedTime() >= 18;

                if (isFullDraw) {
                    final BowConfig settings = plugin.getConfigHandler().getBowConfig();

                    // Play charge sounds ONCE upon reaching full draw (>= 18 ticks of continuous right-click)
                    if (!chargeSoundPlayed.getOrDefault(uuid, false)) {
                        chargeSoundPlayed.put(uuid, true);
                        playSound(player.getLocation(), settings.ultimateChargeSound, 1.0f, 1.0f);
                        playSound(player.getLocation(), settings.ultimateCustomChargeSound, 1.0f, 1.0f);
                    }

                    // Spawn or update face spiral entity (1m in front of eyes)
                    final Location spiralLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0));
                    spiralLoc.setYaw(player.getEyeLocation().getYaw());
                    spiralLoc.setPitch(player.getEyeLocation().getPitch());

                    ItemDisplay spiral = activeAimSpirals.get(uuid);
                    if (spiral == null || !spiral.isValid()) {
                        try {
                            final Material mat = Material.valueOf(settings.beamMaterial);
                            spiral = player.getWorld().spawn(spiralLoc, ItemDisplay.class, display -> {
                                final ItemStack item = new ItemStack(mat, 1);
                                final ItemMeta meta = item.getItemMeta();
                                if (meta != null) {
                                    meta.setCustomModelData(settings.beamSpiralCustomModelData);
                                    item.setItemMeta(meta);
                                }
                                display.setItemStack(item);
                                display.setTransformation(new Transformation(
                                    new Vector3f(0),
                                    new Quaternionf(),
                                    new Vector3f((float) settings.ultimateWidth * 2.0f, (float) settings.ultimateWidth * 2.0f, 0.01f),
                                    new Quaternionf()
                                ));
                            });
                            activeAimSpirals.put(uuid, spiral);
                        } catch (Exception ignored) {}
                    } else {
                        spiral.teleport(spiralLoc);
                    }

                    // Aiming Laser Guide particles
                    final Location start = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0));
                    final Vector dir = player.getEyeLocation().getDirection().normalize();
                    for (double d = 0.0; d < settings.ultimateRange; d += 0.5) {
                        final Location p = start.clone().add(dir.clone().multiply(d));
                        p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p, 1, 0.0, 0.0, 0.0, 0.0);
                    }
                    final Location end = start.clone().add(dir.clone().multiply(settings.ultimateRange));
                    end.getWorld().spawnParticle(Particle.END_ROD, end, 2, 0.1, 0.1, 0.1, 0.01);
                } else {
                    final ItemDisplay spiral = activeAimSpirals.remove(uuid);
                    if (spiral != null) spiral.remove();
                    chargeSoundPlayed.remove(uuid);
                }
            }
        }, 1L, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onEntityShootBowUltimate(@NotNull EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player shooter)) {
            return;
        }

        final UUID uuid = shooter.getUniqueId();
        final int remaining = remainingUltShots.getOrDefault(uuid, 0);
        if (remaining <= 0) {
            return;
        }

        if (event.getBow() == null || event.getBow().getType() != Material.BOW) {
            return;
        }

        if (event.getForce() < 0.8f) {
            return;
        }

        // Cancel arrow launch and item consumption
        event.setCancelled(true);

        final ItemDisplay activeSpiral = activeAimSpirals.remove(uuid);
        chargeSoundPlayed.remove(uuid);

        final BowConfig settings = plugin.getConfigHandler().getBowConfig();
        final int left = remainingUltShots.merge(uuid, -1, Integer::sum);
        if (left <= 0) {
            remainingUltShots.remove(uuid);
        }

        // Trigger 1 Bow Beam shot task, passing active spiral entity so it continues animating alongside main beam display
        new lunatech.strength.task.BowBeamTask(shooter, settings, activeSpiral)
            .runTaskTimer(plugin, 0L, 1L);

        shooter.sendMessage(ColorParser.of(
            "<gold><bold>Fired Bow Beam!</bold> (" + Math.max(0, left) + "/" + settings.ultimateBeams + " shots remaining)</gold>"
        ).build());
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

        // WorldGuard region check for weapon ability
        if (!lunatech.strength.integration.WorldGuardHook.isAbilityAllowed(plugin, shooter, shooter.getLocation())) {
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
                    final lunatech.strength.config.BowConfig bowCfg = plugin.getConfigHandler().getBowConfig();
                    try {
                        org.bukkit.Material mat = org.bukkit.Material.matchMaterial(bowCfg.passiveTrailParticleMaterial);
                        if (mat == null) mat = org.bukkit.Material.COBWEB;

                        final String typeStr = bowCfg.passiveTrailParticleType != null ? bowCfg.passiveTrailParticleType.toUpperCase() : "ITEM";

                        if ("CLOUD".equals(typeStr)) {
                            arrow.getWorld().spawnParticle(Particle.CLOUD, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.02);
                        } else if ("POOF".equals(typeStr)) {
                            arrow.getWorld().spawnParticle(Particle.POOF, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.02);
                        } else {
                            arrow.getWorld().spawnParticle(Particle.ITEM, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.02, new org.bukkit.inventory.ItemStack(mat));
                        }
                    } catch (Throwable ignored) {
                        arrow.getWorld().spawnParticle(Particle.ITEM, arrow.getLocation(), 2, 0.05, 0.05, 0.05, 0.02, new org.bukkit.inventory.ItemStack(org.bukkit.Material.COBWEB));
                    }
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
        remainingUltShots.remove(uuid);
        final ItemDisplay spiral = activeAimSpirals.remove(uuid);
        if (spiral != null) spiral.remove();
        chargeSoundPlayed.remove(uuid);
    }

    private static void playSound(@NotNull Location loc, @NotNull String soundKey, float volume, float pitch) {
        if (soundKey == null || soundKey.isBlank() || "NONE".equalsIgnoreCase(soundKey)) return;
        try {
            final Sound sound = Sound.valueOf(soundKey.toUpperCase());
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (Throwable t) {
            loc.getWorld().playSound(loc, soundKey, volume, pitch);
        }
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

        for (ItemDisplay spiral : activeAimSpirals.values()) {
            if (spiral != null && spiral.isValid()) spiral.remove();
        }
        activeAimSpirals.clear();
        chargeSoundPlayed.clear();
    }
}
