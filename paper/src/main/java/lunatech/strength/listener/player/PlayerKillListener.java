package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.ConfigHandler;
import lunatech.strength.config.PluginConfig.MessagesConfig;
import lunatech.strength.config.PluginConfig.StrengthSettings;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Listener that awards strength to killers and deducts strength from victims.
 */
public final class PlayerKillListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;
    private final ConfigHandler configHandler;

    public PlayerKillListener(@NotNull Strength plugin, @NotNull StrengthService strengthService, @NotNull ConfigHandler configHandler) {
        this.plugin = plugin;
        this.strengthService = strengthService;
        this.configHandler = configHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        final Player victim = event.getEntity();
        final Player killer = victim.getKiller();
        final StrengthSettings settings = configHandler.getConfig().strength;
        final MessagesConfig messages = configHandler.getConfig().messages;

        final boolean isPvp = killer != null && !killer.getUniqueId().equals(victim.getUniqueId());

        // WorldGuard region check: if strength loss is disabled in this region, bypass death processing
        if (isPvp && plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!lunatech.strength.integration.WorldGuardHook.isPvPLossAllowed(plugin, victim, victim.getLocation())) {
                return;
            }
        }

        final lunatech.strength.config.RulesConfig.NakedPlayerRules nakedRules = configHandler.getRulesConfig().nakedPlayer;
        final boolean isVictimNaked = isPvp && isNakedPlayer(victim, nakedRules);
        final boolean allowNakedReward = !isVictimNaked || nakedRules.allowNakedKillReward;

        final boolean shouldProcessDeathLoss = settings.deathLoss > 0 && (isPvp || settings.loseStrengthOnNaturalDeath);

        int actualLoss = 0;

        // Apply death loss if configured and allowed by cause
        if (shouldProcessDeathLoss) {
            final int victimOldStrength = strengthService.getStrength(victim);
            final int victimNewStrength = Math.max(settings.minStrength, victimOldStrength - settings.deathLoss);
            actualLoss = victimOldStrength - victimNewStrength;

            if (actualLoss > 0) {
                strengthService.setStrength(victim, victimNewStrength);

                // Drop strength item on death if enabled, actual loss > 0, and allowed by naked player rule
                if (settings.dropItemOnDeath && allowNakedReward) {
                    event.getDrops().add(strengthService.createStrengthItem(actualLoss));
                }

                lunatech.strength.utility.MessageUtil.send(
                    victim,
                    messages.deathLossMessage,
                    Map.of("loss", String.valueOf(actualLoss), "strength", String.valueOf(victimNewStrength))
                );
            }
        }

        // Award kill reward to the killer in PvP
        if (isPvp) {
            if (!allowNakedReward) {
                killer.sendMessage(
                    ColorParser.of(nakedRules.nakedKillNoRewardMessage)
                        .with("victim", victim.getName())
                        .build()
                );
                return;
            }

            final boolean allowReward = !settings.requireVictimStrengthForReward || actualLoss > 0;

            if (allowReward) {
                // Prevent duplication: if item is dropped on death, do not also auto-grant base strength unless explicitly enabled
                if (!settings.dropItemOnDeath || settings.giveDirectRewardWhenItemDropped) {
                    final int killerOldStrength = strengthService.getStrength(killer);
                    final int killerNewStrength = Math.min(settings.maxStrength, killerOldStrength + settings.killReward);
                    strengthService.setStrength(killer, killerNewStrength);

                    lunatech.strength.utility.MessageUtil.send(
                        killer,
                        messages.killRewardMessage,
                        Map.of("reward", String.valueOf(settings.killReward), "victim", victim.getName(), "strength", String.valueOf(killerNewStrength))
                    );
                } else {
                    lunatech.strength.utility.MessageUtil.send(
                        killer,
                        messages.killDroppedItemMessage,
                        Map.of("victim", victim.getName())
                    );
                }
            } else {
                lunatech.strength.utility.MessageUtil.send(
                    killer,
                    messages.killNoStrengthMessage,
                    Map.of("victim", victim.getName())
                );
            }
        }
    }

    private static boolean isNakedPlayer(@NotNull Player player, @NotNull lunatech.strength.config.RulesConfig.NakedPlayerRules rules) {
        if (!rules.enabled) {
            return false;
        }

        final org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();

        // 1. Completely empty inventory check
        if (rules.requireCompletelyEmptyInventory) {
            boolean hasAnyItem = false;
            for (org.bukkit.inventory.ItemStack item : contents) {
                if (item != null && !item.isEmpty()) {
                    hasAnyItem = true;
                    break;
                }
            }
            if (!hasAnyItem) {
                return true;
            }
        }

        // 2. Check armor and weapons
        if (rules.checkArmorAndWeapons) {
            boolean hasGear = false;
            for (org.bukkit.inventory.ItemStack item : contents) {
                if (item != null && !item.isEmpty()) {
                    final String typeName = item.getType().name();
                    for (String keyword : rules.gearKeywords) {
                        if (typeName.contains(keyword.toUpperCase())) {
                            hasGear = true;
                            break;
                        }
                    }
                    if (hasGear) {
                        break;
                    }
                }
            }
            if (!hasGear) {
                return true;
            }
        }

        return false;
    }
}
