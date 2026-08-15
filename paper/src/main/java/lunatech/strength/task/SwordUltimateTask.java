package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.listener.player.SwordAbilityListener;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing the active Sword Ultimate (Dual Wielding): running for the configured
 * duration and safely ending dual wield status upon expiration.
 */
public final class SwordUltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public SwordUltimateTask(@NotNull Player player, @NotNull Strength plugin, int durationSeconds) {
        this.player = player;
        this.plugin = plugin;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public void run() {
        final UUID uuid = player.getUniqueId();

        if (!player.isOnline() || player.isDead() || !SwordAbilityListener.activeDualWield.containsKey(uuid) || elapsedTicks >= durationTicks) {
            SwordAbilityListener.endDualWield(player, plugin);
            cancel();
            return;
        }

        elapsedTicks++;
    }
}
