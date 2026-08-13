package lunatech.strength.task;

import lunatech.strength.listener.player.CrossbowAbilityListener;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task that renders binding particle rings at head and leg areas for an immobilized player,
 * automatically removing the freeze state upon expiration.
 */
public final class CrossbowImmobilizeTask extends BukkitRunnable {
    private final Player victim;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public CrossbowImmobilizeTask(@NotNull Player victim, int durationSeconds) {
        this.victim = victim;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public void run() {
        final UUID uuid = victim.getUniqueId();
        final Location loc = CrossbowAbilityListener.immobilizedPlayers.get(uuid);

        if (!victim.isOnline() || victim.isDead() || loc == null || elapsedTicks >= durationTicks) {
            CrossbowAbilityListener.immobilizedPlayers.remove(uuid);
            cancel();
            return;
        }

        // Zero out velocity every tick to eliminate knockback from hits/explosions
        victim.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

        // Render 2 particle rings (head at +1.8m, legs at +0.2m)
        if (elapsedTicks % 2 == 0) {
            spawnRing(loc.clone().add(0, 1.8, 0));
            spawnRing(loc.clone().add(0, 0.2, 0));
        }

        elapsedTicks++;
    }

    private void spawnRing(Location center) {
        final double radius = 0.6;
        for (int i = 0; i < 8; i++) {
            final double angle = i * (Math.PI / 4.0);
            final double x = center.getX() + radius * Math.cos(angle);
            final double z = center.getZ() + radius * Math.sin(angle);
            center.getWorld().spawnParticle(Particle.CRIT, x, center.getY(), z, 1, 0, 0, 0, 0);
        }
    }
}
