package lunatech.strength.task;

import lunatech.strength.listener.player.CrossbowAbilityListener;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing the immobilization freeze duration and invisible vehicle passenger state for Crossbow Ultimate.
 */
public final class CrossbowImmobilizeTask extends BukkitRunnable {
    private final Player victim;
    private final ArmorStand vehicle;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public CrossbowImmobilizeTask(@NotNull Player victim, @NotNull ArmorStand vehicle, int durationSeconds) {
        this.victim = victim;
        this.vehicle = vehicle;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public void run() {
        final UUID uuid = victim.getUniqueId();
        final Location loc = CrossbowAbilityListener.immobilizedPlayers.get(uuid);

        if (!victim.isOnline() || victim.isDead() || loc == null || elapsedTicks >= durationTicks) {
            CrossbowAbilityListener.immobilizedPlayers.remove(uuid);
            cleanupVehicle();
            cancel();
            return;
        }

        // Re-mount victim if dismounted unexpectedly
        if (vehicle.isValid() && !vehicle.getPassengers().contains(victim)) {
            vehicle.addPassenger(victim);
        }

        // Zero out velocity every tick to eliminate knockback from hits/explosions
        victim.setVelocity(new org.bukkit.util.Vector(0, 0, 0));

        elapsedTicks++;
    }

    private void cleanupVehicle() {
        if (vehicle != null && vehicle.isValid()) {
            vehicle.removePassenger(victim);
            vehicle.remove();
        }
    }
}
