package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.listener.player.Trident2AbilityListener;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing the active Trident2 Ultimate (Thunderstorm Domain): maintaining world weather
 * and particle effects for the configured duration.
 */
public final class Trident2UltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public Trident2UltimateTask(@NotNull Player player, @NotNull Strength plugin, int durationSeconds) {
        this.player = player;
        this.plugin = plugin;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public void run() {
        final UUID uuid = player.getUniqueId();

        if (!player.isOnline() || player.isDead() || elapsedTicks >= durationTicks) {
            Trident2AbilityListener.endUltimate(player, plugin);
            cancel();
            return;
        }

        final World world = player.getWorld();

        // Periodically enforce thunderstorm weather for the world during ultimate
        if (elapsedTicks % 20 == 0) {
            world.setThundering(true);
            world.setThunderDuration(40);
        }

        // Ambient storm particles around player every 5 ticks
        if (elapsedTicks % 5 == 0) {
            world.spawnParticle(
                Particle.ELECTRIC_SPARK,
                player.getLocation().add(0, 1.2, 0),
                6, 0.5, 0.8, 0.5, 0.1
            );
        }

        elapsedTicks++;
    }
}
