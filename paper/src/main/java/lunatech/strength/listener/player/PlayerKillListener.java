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

        final boolean isPvp = killer != null && !killer.getUniqueId().equals(victim.getUniqueId());
        final boolean shouldProcessDeathLoss = settings.deathLoss > 0 && (isPvp || settings.loseStrengthOnNaturalDeath);

        int actualLoss = 0;

        // Apply death loss if configured and allowed by cause
        if (shouldProcessDeathLoss) {
            final int victimOldStrength = strengthService.getStrength(victim);
            final int victimNewStrength = Math.max(settings.minStrength, victimOldStrength - settings.deathLoss);
            actualLoss = victimOldStrength - victimNewStrength;

            if (actualLoss > 0) {
                strengthService.setStrength(victim, victimNewStrength);

                // Drop strength item on death if enabled and actual loss > 0
                if (settings.dropItemOnDeath) {
                    event.getDrops().add(strengthService.createStrengthItem(actualLoss));
                }

                victim.sendMessage(
                    ColorParser.of("<red>You lost <loss> Strength on death. (New Strength: <strength>)")
                        .with("loss", String.valueOf(actualLoss))
                        .with("strength", String.valueOf(victimNewStrength))
                        .build()
                );
            }
        }

        // Award kill reward to the killer in PvP
        if (isPvp) {
            final boolean allowReward = !settings.requireVictimStrengthForReward || actualLoss > 0;

            if (allowReward) {
                // Prevent duplication: if item is dropped on death, do not also auto-grant base strength unless explicitly enabled
                if (!settings.dropItemOnDeath || settings.giveDirectRewardWhenItemDropped) {
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
                } else {
                    killer.sendMessage(
                        ColorParser.of("<green>You killed <victim>! A Strength Shard has dropped on the ground!</green>")
                            .with("victim", victim.getName())
                            .build()
                    );
                }
            } else {
                killer.sendMessage(
                    ColorParser.of("<yellow><victim> had no Strength to lose, so no Strength was gained!</yellow>")
                        .with("victim", victim.getName())
                        .build()
                );
            }
        }
    }
}
