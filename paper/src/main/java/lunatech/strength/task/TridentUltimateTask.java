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
            final org.bukkit.util.Vector forwardDir = player.getLocation().getDirection().setY(0).normalize();
            final Location spawnLoc = player.getLocation().add(0, 1.2, 0).add(forwardDir.clone().multiply(4.5));
            spawnLoc.setYaw((float) (player.getLocation().getYaw() + settings.modelYawOffsetDegrees));
            spawnFmmModel(settings.barrageModelId, spawnLoc);
            tryPlaySound("thunder_ronin_sounds:samus.thunder_ronin.thunder_barrage", Sound.ITEM_TRIDENT_THROW, 0.7f, 1.0f);
        }

        // 3. Multi-Thrust Damage Loop (9 hits spaced every configured interval ticks)
        final int interval = Math.max(1, settings.lightningStrikeIntervalTicks);
        if (elapsedTicks >= 8 && (elapsedTicks - 8) % interval == 0) {
            final Location center = player.getLocation();
            final double coneRadius = settings.ultimateRadius;

            // Damage forward cone targets
            for (LivingEntity target : center.getWorld().getNearbyLivingEntities(center, coneRadius)) {
                if (target.equals(player) || target instanceof ArmorStand) {
                    continue;
                }

                // Verify target is in forward hemisphere cone
                final Location targetLoc = target.getLocation();
                if (center.getDirection().dot(targetLoc.toVector().subtract(center.toVector()).normalize()) < 0.2) {
                    continue;
                }

                // Deal barrage thrust damage
                target.damage(settings.ultimateDamage, player);

                // Spawn FMM impact VFX model on struck target
                spawnFmmImpactModel(settings.impactModelId, targetLoc.clone().add(0, 0.95, 0));
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
