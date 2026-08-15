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

        // Offhand Attack Cooldown Actionbar Indicator during Dual Wield state
        final Long lastOffhandAttack = SwordAbilityListener.lastOffhandAttackTimes.get(uuid);
        if (lastOffhandAttack != null) {
            final long now = System.currentTimeMillis();
            final long diff = now - lastOffhandAttack;
            final long cooldownMs = 500; // 500ms attack cooldown (+100% attack speed)

            if (diff < cooldownMs) {
                final double pct = (double) diff / (double) cooldownMs;
                final int filled = (int) (pct * 8.0);
                final String bar = "■".repeat(filled) + "□".repeat(8 - filled);
                player.sendActionBar(io.github.milkdrinkers.colorparser.paper.ColorParser.of("<gray>Offhand: <gold>" + bar + "</gold></gray>").build());
            } else if (diff < cooldownMs + 300) {
                player.sendActionBar(io.github.milkdrinkers.colorparser.paper.ColorParser.of("<green><bold>⚔ OFFHAND READY</bold></green>").build());
            }
        }

        elapsedTicks++;
    }
}
