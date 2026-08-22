package lunatech.strength.listener.plugin;

import me.chancesd.pvpmanager.event.PlayerCombatLogEvent;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.MessagesConfig;
import lunatech.strength.config.PluginConfig.StrengthSettings;
import lunatech.strength.service.StrengthService;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that hooks into PvPManager events to penalize combat loggers and enforce PvP rules.
 */
public final class PvPManagerListener implements Listener {
    private final Strength plugin;

    public PvPManagerListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCombatLog(@NotNull PlayerCombatLogEvent event) {
        if (!plugin.getConfigHandler().getConfig().pvpmanager.enabled) {
            return;
        }

        if (!plugin.getConfigHandler().getConfig().pvpmanager.handleCombatLogPenalty) {
            return;
        }

        final Player player = event.getPlayer();
        if (player == null) {
            return;
        }

        final StrengthService strengthService = plugin.getStrengthService();
        final StrengthSettings strengthSettings = plugin.getConfigHandler().getConfig().strength;
        final MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        final int currentStrength = strengthService.getStrength(player);
        final int loss = Math.min(currentStrength - strengthSettings.minStrength, strengthSettings.deathLoss);

        if (loss > 0) {
            final int newStrength = currentStrength - loss;
            strengthService.setStrength(player, newStrength);

            MessageUtil.send(
                player,
                messages.combatLogPenaltyMessage,
                "loss", String.valueOf(loss)
            );
        }
    }
}
