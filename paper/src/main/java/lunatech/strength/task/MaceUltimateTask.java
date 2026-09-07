package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.listener.player.MaceAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task executing Mace Ultimate ability (Cataclysmic Slam).
 * Launches player into the air and monitors ground impact to trigger a high-impact explosion shockwave.
 */
public final class MaceUltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final MaceConfig.UltimateConfig settings;
    private int ticksPassed = 0;
    private boolean leftGround = false;

    public MaceUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull MaceConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
    }

    public void launch() {
        // Apply leap velocity
        final Vector currentVel = player.getVelocity();
        player.setVelocity(new Vector(currentVel.getX() * 0.5, settings.leapVelocity, currentVel.getZ() * 0.5));

        final Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, Sound.ITEM_MACE_SMASH_AIR, 1.5f, 0.9f);
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (settings.ultimateActivatedMessage != null && !settings.ultimateActivatedMessage.isBlank()) {
            player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
        }

        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        ticksPassed++;
        final UUID uuid = player.getUniqueId();

        if (!player.isOnline() || player.isDead() || ticksPassed > 100) {
            MaceAbilityListener.activeUltimatePlayers.remove(uuid);
            cancel();
            return;
        }

        // Particle trail during leap / fall
        final Location loc = player.getLocation();
        if (loc.getWorld() != null && ticksPassed % 2 == 0) {
            loc.getWorld().spawnParticle(Particle.CLOUD, loc, 3, 0.2, 0.2, 0.2, 0.02);
        }

        // Detect leaving ground and subsequent landing
        final boolean onGround = player.isOnGround() || player.getLocation().getBlock().getType().isSolid();
        if (!leftGround) {
            if (!onGround || ticksPassed > 5) {
                leftGround = true;
            }
            return;
        }

        if (onGround && ticksPassed > 5) {
            // Impact! Cataclysmic Slam
            executeCataclysmicSlam(loc);
            MaceAbilityListener.activeUltimatePlayers.remove(uuid);
            cancel();
        }
    }

    private void executeCataclysmicSlam(Location impactLoc) {
        if (impactLoc.getWorld() == null) return;

        // Visual and sound effects
        impactLoc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, impactLoc, 2, 0.5, 0.2, 0.5, 0.0);
        impactLoc.getWorld().spawnParticle(Particle.FLAME, impactLoc.clone().add(0, 0.5, 0), 40, 1.5, 0.5, 1.5, 0.15);
        impactLoc.getWorld().playSound(impactLoc, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 2.0f, 0.7f);
        impactLoc.getWorld().playSound(impactLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);

        final double radius = settings.slamRadius;
        final double damage = settings.slamDamage;

        // Damage and launch surrounding entities
        for (Entity entity : impactLoc.getWorld().getNearbyEntities(impactLoc, radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !target.equals(player)) {
                target.damage(damage, player);

                // Knockback / Launch effect away from impact center
                Vector launchDir = target.getLocation().toVector().subtract(impactLoc.toVector());
                if (launchDir.lengthSquared() < 0.01) {
                    launchDir = new Vector(0, 1, 0);
                } else {
                    launchDir.normalize().multiply(1.2).setY(0.7);
                }
                target.setVelocity(launchDir);
            }
        }
    }
}
