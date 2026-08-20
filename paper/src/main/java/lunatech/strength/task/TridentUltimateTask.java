package lunatech.strength.task;

import lunatech.strength.config.TridentConfig;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LightningBolt;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Task that manages Poseidon's Calling Ultimate ability:
 * Slams trident, summoning lightning strikes around the player hitting all enemies within radius,
 * dealing shock damage and applying slowness.
 */
public final class TridentUltimateTask extends BukkitRunnable {
    private final Player player;
    private final TridentConfig settings;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public TridentUltimateTask(@NotNull Player player, @NotNull TridentConfig settings) {
        this.player = player;
        this.settings = settings;
        this.durationTicks = settings.ultimateDurationTicks;
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || elapsedTicks >= durationTicks) {
            cancel();
            return;
        }

        // Start of ability sound effects
        if (elapsedTicks == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 5.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_THUNDER, 3.0f, 1.0f);
        }

        // Trigger lightning strikes at configured interval ticks
        final int interval = Math.max(1, settings.lightningStrikeIntervalTicks);
        if (elapsedTicks % interval == 0) {
            final double radius = settings.ultimateRadius;
            final Location center = player.getLocation();
            final Particle.DustOptions yellowDust = new Particle.DustOptions(Color.fromRGB(255, 220, 0), 1.8f);

            for (Entity entity : center.getWorld().getNearbyEntities(center, radius, radius, radius)) {
                if (!(entity instanceof LivingEntity target) || entity.equals(player) || entity instanceof ArmorStand) {
                    continue;
                }

                final Location targetLoc = target.getLocation();

                // 1. Visual-only lightning bolt strike (Paper API native)
                center.getWorld().spawn(targetLoc, LightningBolt.class, bolt -> bolt.setVisualOnly(true));

                // 2. Yellow particle effects (RGB 255,220,0 dust + electric spark)
                center.getWorld().spawnParticle(Particle.DUST, targetLoc.clone().add(0, 1, 0), 20, 0.4, 0.8, 0.4, yellowDust);
                center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, targetLoc, 25, 0.3, 0.5, 0.3, 0.1);

                // 3. Optional 3D yellow lightning item display entity
                if (settings.yellowLightningCustomModelData > 0) {
                    center.getWorld().spawn(targetLoc, ItemDisplay.class, display -> {
                        final ItemStack item = new ItemStack(Material.NAUTILUS_SHELL);
                        final ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setCustomModelData(settings.yellowLightningCustomModelData);
                            item.setItemMeta(meta);
                        }
                        display.setItemStack(item);
                        display.setBillboard(ItemDisplay.Billboard.CENTER);
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                            org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass()),
                            display::remove,
                            15L
                        );
                    });
                }

                // 4. Apply damage and Slowness effect
                target.damage(settings.ultimateDamage, player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, settings.slownessDurationTicks, settings.slownessAmplifier));
                target.getWorld().playSound(targetLoc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 1.0f);
            }
        }

        elapsedTicks++;
    }
}
