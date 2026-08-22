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

        // Offhand Attack Cooldown Actionbar Indicator during Dual Wield state (1:1 dynamic & configurable)
        final Long lastOffhandAttack = SwordAbilityListener.lastOffhandAttackTimes.get(uuid);
        final lunatech.strength.config.SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
        if (lastOffhandAttack != null) {
            final long now = System.currentTimeMillis();
            final long diff = now - lastOffhandAttack;
            final org.bukkit.attribute.AttributeInstance speedAttr = player.getAttribute(org.bukkit.attribute.Attribute.ATTACK_SPEED);
            final double attackSpeed = (speedAttr != null) ? speedAttr.getValue() : 8.0;
            final long cooldownMs = (long) (1000.0 / Math.max(1.0, attackSpeed));

            if (diff < cooldownMs) {
                final double pct = Math.min(1.0, Math.max(0.0, (double) diff / (double) cooldownMs));
                final int filled = (int) (pct * 8.0);
                final String bar = "■".repeat(filled) + "□".repeat(8 - filled);
                if (settings.offhandChargingActionbarMessage != null && !settings.offhandChargingActionbarMessage.trim().isEmpty()) {
                    player.sendActionBar(
                        io.github.milkdrinkers.colorparser.paper.ColorParser.of(settings.offhandChargingActionbarMessage
                            .replace("{bar}", bar)
                            .replace("<bar>", bar))
                            .with("bar", bar)
                            .build()
                    );
                }
            } else if (diff < cooldownMs + 300) {
                if (settings.offhandReadyActionbarMessage != null && !settings.offhandReadyActionbarMessage.trim().isEmpty()) {
                    player.sendActionBar(io.github.milkdrinkers.colorparser.paper.ColorParser.of(settings.offhandReadyActionbarMessage).build());
                }
            }
        }

        elapsedTicks++;
    }
}
