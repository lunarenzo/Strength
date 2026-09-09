package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.TridentConfig;
import lunatech.strength.hook.betterteams.BetterTeamsHook;
import lunatech.strength.integration.WorldGuardHook;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.UUID;

/**
 * Task managing active Trident Ultimate (Thunderous Barrage):
 * Rapid forward thrust strikes piercing enemies with high speed, applying slowness to caster,
 * spawning custom 3D FreeMinecraftModels VFX, and playing custom audio.
 */
public final class TridentUltimateTask extends BukkitRunnable {
    // Pre-cached static particle transition to eliminate GC heap allocations in the 20 Hz tick loop
    private static final Particle.DustTransition RING_DUST_TRANSITION =
        new Particle.DustTransition(Color.fromRGB(243, 255, 178), Color.fromRGB(50, 255, 211), 1.2f);

    private static final String BARRAGE_ACTIVE_KEY = "trident_barrage_active";

    // Pre-cached static MethodHandles for FMM dynamic reflection (< 0.001 mspt overhead)
    private static MethodHandle FMM_CREATE_HANDLE;
    private static MethodHandle FMM_PLAY_ANIMATION_HANDLE;
    private static MethodHandle FMM_TELEPORT_HANDLE;
    private static MethodHandle FMM_REMOVE_HANDLE;

    static {
        try {
            final Class<?> staticEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.StaticEntity");
            final MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            FMM_CREATE_HANDLE = lookup.findStatic(staticEntityClass, "create",
                MethodType.methodType(staticEntityClass, String.class, Location.class));

            FMM_PLAY_ANIMATION_HANDLE = lookup.findVirtual(staticEntityClass, "playAnimation",
                MethodType.methodType(boolean.class, String.class, boolean.class, boolean.class));

            FMM_TELEPORT_HANDLE = lookup.findVirtual(staticEntityClass, "teleport",
                MethodType.methodType(void.class, Location.class, boolean.class));

            FMM_REMOVE_HANDLE = lookup.findVirtual(staticEntityClass, "remove",
                MethodType.methodType(void.class));
        } catch (ReflectiveOperationException ignored) {
            // FreeMinecraftModels not present or API signature changed
        }
    }

    private final Player player;
    private final Strength plugin;
    private final TridentConfig.UltimateConfig settings;
    private final int durationTicks;
    private int elapsedTicks;
    private int executedHits;

    // FMM model reference stored dynamically to avoid hard compile classloading dependency failures
    private Object barrageFmmModel;

    public TridentUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull TridentConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
        this.durationTicks = settings.durationTicks;
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || elapsedTicks >= durationTicks) {
            cleanup();
            cancel();
            return;
        }

        // 1. Initial Telegraph & Slowness (Ticks 0..7)
        if (elapsedTicks == 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, settings.slownessDurationTicks, settings.slownessAmplifier, false, false));
            tryPlaySound("thunder_ronin_sounds:samus.thunder_ronin.thunder_circle_charge", Sound.ENTITY_PLAYER_ATTACK_SWEEP, 0.7f, 1.0f);
        }

        // Collapsing teal/yellow particle ring during chargeup (ticks 0..7) - Zero allocation loop
        if (elapsedTicks < 8) {
            final double radius = Math.max(1.0, 4.0 - (elapsedTicks * 0.45));
            final Location loc = player.getLocation();
            final double baseWorldX = loc.getX();
            final double baseWorldY = loc.getY() + 0.1;
            final double baseWorldZ = loc.getZ();
            final World world = loc.getWorld();

            if (world != null) {
                for (int i = 0; i < 40; i++) {
                    final double angle = (2 * Math.PI / 40.0) * i;
                    final double px = baseWorldX + (radius * Math.cos(angle));
                    final double pz = baseWorldZ + (radius * Math.sin(angle));
                    world.spawnParticle(Particle.DUST_COLOR_TRANSITION, px, baseWorldY, pz, 1, 0, 0, 0, 0, RING_DUST_TRANSITION);
                }
            }
        }

        // 2. Spawn FMM Model & Start Barrage Audio (Tick 8)
        if (elapsedTicks == 8) {
            final Plugin pluginInstance = JavaPlugin.getProvidingPlugin(getClass());
            player.setMetadata(BARRAGE_ACTIVE_KEY, new FixedMetadataValue(pluginInstance, true));
            final Vector forwardDir = player.getLocation().getDirection().setY(0).normalize();
            final Location spawnLoc = player.getLocation().add(0, 1.2, 0).add(forwardDir.clone().multiply(1.5));
            spawnLoc.setYaw((float) (player.getLocation().getYaw() + settings.modelYawOffsetDegrees));
            spawnFmmModel(settings.barrageModelId, spawnLoc);
            tryPlaySound("thunder_ronin_sounds:samus.thunder_ronin.thunder_barrage", Sound.ITEM_TRIDENT_THROW, 0.7f, 1.0f);
        }

        // Keep 3D model attached to player as player moves or turns (1:1 smooth pitch & yaw tracking via MethodHandle)
        if (elapsedTicks >= 8 && barrageFmmModel != null && FMM_TELEPORT_HANDLE != null) {
            try {
                final Vector dir = player.getLocation().getDirection().normalize();
                final Location currentModelLoc = player.getLocation().add(0, 1.2, 0).add(dir.clone().multiply(1.5));
                currentModelLoc.setYaw((float) (player.getLocation().getYaw() + settings.modelYawOffsetDegrees));
                currentModelLoc.setPitch(player.getLocation().getPitch());
                FMM_TELEPORT_HANDLE.invoke(barrageFmmModel, currentModelLoc, false);
            } catch (Throwable ignored) {
            }
        }

        // 3. Multi-Thrust Damage Loop (up to maxBarrageHits spaced every configured interval ticks)
        final int interval = Math.max(1, settings.strikeIntervalTicks);
        if (elapsedTicks >= 8 && (elapsedTicks - 8) % interval == 0 && executedHits < settings.maxBarrageHits) {
            executedHits++;
            final double reachLength = settings.radius;
            final double halfWidth = settings.widthBlocks / 2.0;

            // Horizontal forward look vector (ignoring pitch skew for strict box projection)
            final Vector forwardDir = player.getLocation().getDirection().setY(0);
            if (forwardDir.lengthSquared() < 1e-6) {
                forwardDir.setX(1).setY(0).setZ(0);
            } else {
                forwardDir.normalize();
            }

            // Perpendicular horizontal right vector (90 deg to right of forward look)
            final Vector rightDir = new Vector(-forwardDir.getZ(), 0, forwardDir.getX()).normalize();

            // Real-time damage box raycast following player camera direction
            final World world = player.getWorld();
            for (LivingEntity target : world.getNearbyLivingEntities(player.getLocation(), reachLength + 1.0)) {
                if (target.equals(player) || target instanceof ArmorStand || !target.isValid() || target.isDead()) {
                    continue;
                }

                // WorldGuard region check for target location
                if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                    if (!WorldGuardHook.isAbilityAllowed(plugin, player, target.getLocation())) {
                        continue;
                    }
                }

                // Vector from player feet to target feet
                final Vector toTarget = target.getLocation().toVector().subtract(player.getLocation().toVector());

                // 1. Forward projection length check (must be strictly in front of player between 0.1 and reachLength)
                final double forwardDist = toTarget.dot(forwardDir);
                if (forwardDist < 0.1 || forwardDist > reachLength) {
                    continue;
                }

                // 2. Lateral width check (must be within halfWidth to left or right of center line)
                final double lateralDist = Math.abs(toTarget.dot(rightDir));
                if (lateralDist > halfWidth) {
                    continue;
                }

                // 3. Vertical height check (+/- 2.5 blocks)
                final double verticalDist = Math.abs(target.getLocation().getY() - player.getLocation().getY());
                if (verticalDist > 2.5) {
                    continue;
                }

                if (target instanceof Player targetPlayer && !BetterTeamsHook.canDamage(player, targetPlayer)) {
                    continue;
                }

                // Save velocity before damage to prevent knockback
                final Vector preVel = target.getVelocity().clone();

                // Bypass invulnerability frames matching MythicMobs hnp=true
                target.setMaximumNoDamageTicks(0);
                target.setNoDamageTicks(0);

                // Deal barrage thrust damage
                target.damage(settings.damage * plugin.getStrengthService().getScale(), player);
                target.setNoDamageTicks(0);

                // Cancel Spigot knockback impulse on next tick
                final LivingEntity finalTarget = target;
                final Plugin pluginInstance = JavaPlugin.getProvidingPlugin(getClass());
                Bukkit.getScheduler().runTaskLater(
                    pluginInstance,
                    () -> {
                        if (finalTarget.isValid() && !finalTarget.isDead()) {
                            finalTarget.setVelocity(preVel);
                        }
                    },
                    1L
                );

                // Spawn FMM impact VFX model on struck target
                spawnFmmImpactModel(settings.impactModelId, target.getLocation().add(0, 0.95, 0));
            }
        }

        elapsedTicks++;
    }

    private void tryPlaySound(String customSoundKey, Sound fallbackSound, float volume, float pitch) {
        try {
            player.getWorld().playSound(player.getLocation(), customSoundKey, volume, pitch);
        } catch (Exception t) {
            player.getWorld().playSound(player.getLocation(), fallbackSound, volume, pitch);
        }
    }

    private void spawnFmmModel(String modelId, Location location) {
        if (FMM_CREATE_HANDLE == null || FMM_PLAY_ANIMATION_HANDLE == null) {
            return;
        }
        try {
            final Object model = FMM_CREATE_HANDLE.invoke(modelId, location);
            if (model != null) {
                final boolean success = (boolean) FMM_PLAY_ANIMATION_HANDLE.invoke(model, "skill", false, false);
                if (!success) {
                    FMM_PLAY_ANIMATION_HANDLE.invoke(model, "animation", false, false);
                }
                this.barrageFmmModel = model;
            }
        } catch (Throwable ignored) {
            // FreeMinecraftModels missing or model ID not loaded
        }
    }

    private void spawnFmmImpactModel(String impactModelId, Location location) {
        if (FMM_CREATE_HANDLE == null || FMM_PLAY_ANIMATION_HANDLE == null || FMM_REMOVE_HANDLE == null) {
            return;
        }
        try {
            final Object impact = FMM_CREATE_HANDLE.invoke(impactModelId, location);
            if (impact != null) {
                try {
                    final boolean success = (boolean) FMM_PLAY_ANIMATION_HANDLE.invoke(impact, "animation", false, false);
                    if (!success) {
                        FMM_PLAY_ANIMATION_HANDLE.invoke(impact, "skill", false, false);
                    }
                } catch (Throwable ignored) {
                }
                final Plugin pluginInstance = JavaPlugin.getProvidingPlugin(getClass());
                Bukkit.getScheduler().runTaskLater(
                    pluginInstance,
                    () -> {
                        try {
                            FMM_REMOVE_HANDLE.invoke(impact);
                        } catch (Throwable ignored) {
                        }
                    },
                    6L
                );
            }
        } catch (Throwable ignored) {
            // FreeMinecraftModels missing or impact model not loaded
        }
    }

    private void cleanup() {
        if (player.hasMetadata(BARRAGE_ACTIVE_KEY)) {
            final Plugin pluginInstance = JavaPlugin.getProvidingPlugin(getClass());
            player.removeMetadata(BARRAGE_ACTIVE_KEY, pluginInstance);
        }
        if (barrageFmmModel != null && FMM_REMOVE_HANDLE != null) {
            try {
                FMM_REMOVE_HANDLE.invoke(barrageFmmModel);
            } catch (Throwable ignored) {
            }
            barrageFmmModel = null;
        }
    }
}
