package lunatech.strength.task;

import lunatech.strength.listener.player.CrossbowAbilityListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing the immobilization freeze duration for a player struck by Crossbow Ultimate.
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

        elapsedTicks++;
    }
}
