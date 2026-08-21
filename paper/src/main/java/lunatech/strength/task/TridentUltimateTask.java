package lunatech.strength.task;

import lunatech.strength.config.TridentConfig;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Task that manages the Thunderous Barrage Ultimate ability for Trident:
 * Rapid forward thrust strikes piercing enemies with high speed, applying slowness to caster
 * and spawning custom 3D FreeMinecraftModels VFX and custom thunder ronin sounds.
 */
public final class TridentUltimateTask extends BukkitRunnable {
    private final Player player;
    private final TridentConfig settings;
    private final int durationTicks;
    private int elapsedTicks = 0;

    // FMM model reference stored dynamically to avoid hard compile classloading dependency failures
    private Object barrageFmmModel = null;

    public TridentUltimateTask(@NotNull Player player, @NotNull TridentConfig settings) {
        this.player = player;
        this.settings = settings;
        this.durationTicks = settings.ultimateDurationTicks;
    }

    private int executedHits = 0;

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

        // Collapsing teal/yellow particle ring during chargeup (ticks 0..7)
        if (elapsedTicks < 8) {
            final double radius = Math.max(1.0, 4.0 - (elapsedTicks * 0.45));
            final Location loc = player.getLocation().add(0, 0.1, 0);
            final Particle.DustTransition ringColor = new Particle.DustTransition(Color.fromRGB(243, 255, 178), Color.fromRGB(50, 255, 211), 1.2f);
            for (int i = 0; i < 40; i++) {
                double angle = (2 * Math.PI / 40) * i;
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                loc.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, loc.clone().add(x, 0, z), 1, 0, 0, 0, 0, ringColor);
            }
        }

        // 2. Spawn FMM Model & Start Barrage Audio (Tick 8)
        if (elapsedTicks == 8) {
            final org.bukkit.plugin.Plugin plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass());
            player.setMetadata("trident_barrage_active", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
            final org.bukkit.util.Vector forwardDir = player.getLocation().getDirection().setY(0).normalize();
            final Location spawnLoc = player.getLocation().add(0, 1.2, 0).add(forwardDir.clone().multiply(1.5));
            spawnLoc.setYaw((float) (player.getLocation().getYaw() + settings.modelYawOffsetDegrees));
            spawnFmmModel(settings.barrageModelId, spawnLoc);
            tryPlaySound("thunder_ronin_sounds:samus.thunder_ronin.thunder_barrage", Sound.ITEM_TRIDENT_THROW, 0.7f, 1.0f);
        }

        // Keep 3D model attached to player as player moves or turns (1:1 smooth pitch & yaw tracking)
        if (elapsedTicks >= 8 && barrageFmmModel != null) {
            try {
                final org.bukkit.util.Vector dir = player.getLocation().getDirection().normalize();
                final Location currentModelLoc = player.getLocation().add(0, 1.2, 0).add(dir.clone().multiply(1.5));
                currentModelLoc.setYaw((float) (player.getLocation().getYaw() + settings.modelYawOffsetDegrees));
                currentModelLoc.setPitch(player.getLocation().getPitch());
                java.lang.reflect.Method teleportMethod = barrageFmmModel.getClass().getMethod("teleport", Location.class, boolean.class);
                teleportMethod.invoke(barrageFmmModel, currentModelLoc, false);
            } catch (Throwable ignored) {}
        }

        // 3. Multi-Thrust Damage Loop (up to maxBarrageHits spaced every configured interval ticks)
        final int interval = Math.max(1, settings.lightningStrikeIntervalTicks);
        if (elapsedTicks >= 8 && (elapsedTicks - 8) % interval == 0 && executedHits < settings.maxBarrageHits) {
            executedHits++;
            final Location eyeLoc = player.getEyeLocation();
            final org.bukkit.util.Vector lookDir = eyeLoc.getDirection().normalize();
            final double coneRadius = settings.ultimateRadius;

            // Real-time damage cone raycast following live camera direction
            for (LivingEntity target : player.getWorld().getNearbyLivingEntities(eyeLoc, coneRadius)) {
                if (target.equals(player) || target instanceof ArmorStand || !target.isValid() || target.isDead()) {
                    continue;
                }

                final org.bukkit.util.Vector toTarget = target.getEyeLocation().toVector().subtract(eyeLoc.toVector());
                final double distance = toTarget.length();
                if (distance > coneRadius || distance < 0.1) {
                    continue;
                }

                // Check forward camera cone alignment (dot product >= 0.4 for ~66-degree forward cone)
                if (lookDir.dot(toTarget.normalize()) < 0.4) {
                    continue;
                }

                // Save velocity before damage to prevent knockback (1:1 MythicMobs pkb=true)
                final org.bukkit.util.Vector preVel = target.getVelocity().clone();

                // Bypass invulnerability frames matching MythicMobs hnp=true (Has No Protection)
                target.setMaximumNoDamageTicks(0);
                target.setNoDamageTicks(0);

                // Deal barrage thrust damage
                target.damage(settings.ultimateDamage, player);
                target.setNoDamageTicks(0);

                // Cancel Spigot knockback impulse on next tick (1:1 MythicMobs pkb=true)
                final LivingEntity finalTarget = target;
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
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
        } catch (Throwable t) {
            player.getWorld().playSound(player.getLocation(), fallbackSound, volume, pitch);
        }
    }

    private void spawnFmmModel(String modelId, Location location) {
        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
            return;
        }
        try {
            Class<?> staticEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.StaticEntity");
            java.lang.reflect.Method createMethod = staticEntityClass.getMethod("create", String.class, Location.class);
            Object model = createMethod.invoke(null, modelId, location);
            if (model != null) {
                java.lang.reflect.Method playAnimMethod = staticEntityClass.getMethod("playAnimation", String.class, boolean.class, boolean.class);
                Object success = playAnimMethod.invoke(model, "skill", false, false);
                if (Boolean.FALSE.equals(success)) {
                    playAnimMethod.invoke(model, "animation", false, false);
                }
                this.barrageFmmModel = model;
            }
        } catch (Throwable ignored) {
            // FreeMinecraftModels missing or model ID not loaded
        }
    }

    private void spawnFmmImpactModel(String impactModelId, Location location) {
        if (!org.bukkit.Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
            return;
        }
        try {
            Class<?> staticEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.StaticEntity");
            java.lang.reflect.Method createMethod = staticEntityClass.getMethod("create", String.class, Location.class);
            Object impact = createMethod.invoke(null, impactModelId, location);
            if (impact != null) {
                try {
                    java.lang.reflect.Method playAnimMethod = staticEntityClass.getMethod("playAnimation", String.class, boolean.class, boolean.class);
                    Object success = playAnimMethod.invoke(impact, "animation", false, false);
                    if (Boolean.FALSE.equals(success)) {
                        playAnimMethod.invoke(impact, "skill", false, false);
                    }
                } catch (Throwable ignored) {}
                org.bukkit.Bukkit.getScheduler().runTaskLater(
                    org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                    () -> {
                        try {
                            java.lang.reflect.Method removeMethod = staticEntityClass.getMethod("remove");
                            removeMethod.invoke(impact);
                        } catch (Throwable ignored) {}
                    },
                    6L
                );
            }
        } catch (Throwable ignored) {
            // FreeMinecraftModels missing or impact model not loaded
        }
    }

    private void cleanup() {
        if (player.hasMetadata("trident_barrage_active")) {
            final org.bukkit.plugin.Plugin plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass());
            player.removeMetadata("trident_barrage_active", plugin);
        }
        if (barrageFmmModel != null) {
            try {
                Class<?> staticEntityClass = Class.forName("com.magmaguy.freeminecraftmodels.customentity.StaticEntity");
                java.lang.reflect.Method removeMethod = staticEntityClass.getMethod("remove");
                removeMethod.invoke(barrageFmmModel);
            } catch (Throwable ignored) {}
            barrageFmmModel = null;
        }
    }
}
