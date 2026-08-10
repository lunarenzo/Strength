package lunatech.strength.listener.player;

import lunatech.strength.config.ConfigHandler;
import lunatech.strength.config.PluginConfig.StrengthSettings;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that awards strength to killers and deducts strength from victims.
 */
public final class PlayerKillListener implements Listener {
    private final StrengthService strengthService;
    private final ConfigHandler configHandler;

    public PlayerKillListener(@NotNull StrengthService strengthService, @NotNull ConfigHandler configHandler) {
        this.strengthService = strengthService;
        this.configHandler = configHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        final Player killer = victim.getKiller();
        final StrengthSettings settings = configHandler.getConfig().strength;

        // Apply death loss if configured
        if (settings.deathLoss > 0) {
            final int victimOldStrength = strengthService.getStrength(victim);
            final int victimNewStrength = Math.max(settings.minStrength, victimOldStrength - settings.deathLoss);
            strengthService.setStrength(victim, victimNewStrength);
            
            victim.sendMessage(
                ColorParser.of("<red>You lost <loss> Strength on death. (New Strength: <strength>)")
                    .with("loss", String.valueOf(settings.deathLoss))
                    .with("strength", String.valueOf(victimNewStrength))
                    .build()
            );
        }

        // Award kill reward to the killer
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            final int killerOldStrength = strengthService.getStrength(killer);
            final int killerNewStrength = Math.min(settings.maxStrength, killerOldStrength + settings.killReward);
            strengthService.setStrength(killer, killerNewStrength);

            killer.sendMessage(
                ColorParser.of("<green>You gained +<reward> Strength for killing <victim>! (New Strength: <strength>)")
                    .with("reward", String.valueOf(settings.killReward))
                    .with("victim", victim.getName())
                    .with("strength", String.valueOf(killerNewStrength))
                    .build()
            );
        }
    }
}
