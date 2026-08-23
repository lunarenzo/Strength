package lunatech.strength.task;

import lunatech.strength.config.BowConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Set;

/**
 * Task that manages the active Bow Ultimate: executing a sequence of sonic charge-ups
 * and firing 3 powerful, spinning item-display beams and spiral displays that damage all entities in their path.
 */
public final class BowBeamTask extends BukkitRunnable {
    private final Player player;
    private final BowConfig settings;
    
    private int currentBeamIndex = 0;
    private int beamTick = 0;
    private ItemDisplay currentBeamEntity = null;
    private ItemDisplay currentSpiralEntity = null;

    public BowBeamTask(@NotNull Player player, @NotNull BowConfig settings) {
        this.player = player;
        this.settings = settings;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cleanup();
            cancel();
            return;
        }

        // 1. Charge Phase (Ticks 0 - 19)
        if (beamTick == 0) {
            playSound(player.getLocation(), settings.ultimateChargeSound, 1.0f, 1.0f);
            playSound(player.getLocation(), settings.ultimateCustomChargeSound, 1.0f, 1.0f);
        }

        if (beamTick < 20) {
            final Location start = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0));
            final Vector dir = player.getEyeLocation().getDirection().normalize();
            for (double d = 0.0; d < settings.ultimateRange; d += 0.5) {
                final Location p = start.clone().add(dir.clone().multiply(d));
                p.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, p, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // 2. Fire Phase (Tick 20)
        else if (beamTick == 20) {
            playSound(player.getLocation(), settings.ultimateFireSound, 1.0f, 1.0f);

            // Calculate center spawn location for the main beam (Z scale mid-point)
            final Location center = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(settings.ultimateRange / 2.0 + 1.5));
            center.setYaw(player.getEyeLocation().getYaw());
            center.setPitch(player.getEyeLocation().getPitch());

            // Calculate spawn location for the face spiral ring (1.0 meter in front of face)
            final Location spiralLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(1.0));
            spiralLoc.setYaw(player.getEyeLocation().getYaw());
            spiralLoc.setPitch(player.getEyeLocation().getPitch());

            try {
                final Material mat = Material.valueOf(settings.beamMaterial);
                
                // Spawn main beam Display
                currentBeamEntity = player.getWorld().spawn(center, ItemDisplay.class, display -> {
                    final ItemStack item = new ItemStack(mat, 1);
                    final ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(settings.beamCustomModelData);
                        item.setItemMeta(meta);
                    }
                    display.setItemStack(item);
                    display.setTransformation(new Transformation(
                        new Vector3f(0),
                        new Quaternionf(),
                        new Vector3f((float) settings.ultimateWidth, (float) settings.ultimateWidth, (float) settings.ultimateRange / 3.0f),
                        new Quaternionf()
                    ));
                });

                // Spawn face spiral Display
                currentSpiralEntity = player.getWorld().spawn(spiralLoc, ItemDisplay.class, display -> {
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
            } catch (Exception e) {
                // Fallback to paper item display if material parsing fails
                currentBeamEntity = player.getWorld().spawn(center, ItemDisplay.class, display -> {
                    display.setItemStack(new ItemStack(Material.PAPER, 1));
                });
            }

            // Deal raycast damage
            final Set<LivingEntity> damaged = new HashSet<>();
            final Location eyeLoc = player.getEyeLocation();
            final Vector beamDir = eyeLoc.getDirection().normalize();
            for (double d = 1.0; d <= settings.ultimateRange; d += 0.5) {
                final Location point = eyeLoc.clone().add(beamDir.clone().multiply(d));
                for (Entity entity : point.getWorld().getNearbyEntities(point, settings.ultimateWidth + 1.0, settings.ultimateWidth + 1.0, settings.ultimateWidth + 1.0)) {
                    if (entity instanceof LivingEntity living && living != player && !(living instanceof ItemDisplay)) {
                        if (damaged.add(living)) {
                            living.damage(settings.ultimateDamage, player);
                            living.getWorld().playSound(living.getLocation(), Sound.ENTITY_GENERIC_HURT, 1.0f, 1.0f);
                        }
                    }
                }
            }
        }

        // 3. Animation Phase (Ticks 21 - 39)
        else if (beamTick > 20 && beamTick < 40) {
            final int animTick = beamTick - 20;
            final float angle = animTick * 12.0f;
            final Quaternionf rot = new Quaternionf().rotateZ((float) Math.toRadians(angle));

            // Pulsing/Tapering scale (shrink to 0 in the last 10 ticks)
            float scale = (float) settings.ultimateWidth;
            if (animTick > 10) {
                scale = (float) settings.ultimateWidth * (1.0f - (animTick - 10) / 10.0f);
            }

            if (currentBeamEntity != null && currentBeamEntity.isValid()) {
                currentBeamEntity.setTransformation(new Transformation(
                    new Vector3f(0),
                    rot,
                    new Vector3f(scale, scale, (float) settings.ultimateRange / 3.0f),
                    new Quaternionf()
                ));
                currentBeamEntity.setInterpolationDuration(1);
                currentBeamEntity.setInterpolationDelay(0);
            }

            if (currentSpiralEntity != null && currentSpiralEntity.isValid()) {
                currentSpiralEntity.setTransformation(new Transformation(
                    new Vector3f(0),
                    rot,
                    new Vector3f(scale * 2.0f, scale * 2.0f, 0.01f),
                    new Quaternionf()
                ));
                currentSpiralEntity.setInterpolationDuration(1);
                currentSpiralEntity.setInterpolationDelay(0);
            }
        }

        // 4. Beam Termination & Loop Advance (Tick 39)
        if (beamTick == 39) {
            cleanup();

            beamTick = -1; // Reset to 0 next tick
            currentBeamIndex++;
            if (currentBeamIndex >= settings.ultimateBeams) {
                cancel();
            }
        }

        beamTick++;
    }

    private void cleanup() {
        if (currentBeamEntity != null) {
            currentBeamEntity.remove();
            currentBeamEntity = null;
        }
        if (currentSpiralEntity != null) {
            currentSpiralEntity.remove();
            currentSpiralEntity = null;
        }
    }

    private void playSound(@NotNull Location loc, @NotNull String soundKey, float volume, float pitch) {
        if (soundKey == null || soundKey.isBlank()) return;
        try {
            final Sound sound = Sound.valueOf(soundKey.toUpperCase());
            loc.getWorld().playSound(loc, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            loc.getWorld().playSound(loc, soundKey, volume, pitch);
        }
    }
}
