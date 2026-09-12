package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import lunatech.strength.utility.ItemResolver;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command class for "/weaponclass roll" that allows unassigned players to roll a new weapon class.
 */
public final class WeaponClassCommand extends Command {
    private final Strength plugin;
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();

    public WeaponClassCommand(@NotNull AbstractStrength plugin) {
        this.plugin = (Strength) plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("weaponclass")
            .withSubcommand(
                new CommandAPICommand("roll")
                    .withHelp("Roll a new weapon class.", "Roll a new weapon class.")
                    .executesPlayer(this::executeRoll)
            );
    }

    private void executeRoll(Player player, CommandArguments args) {
        final PluginConfig config = plugin.getConfigHandler().getConfig();
        final PluginConfig.WeaponSettings settings = config.weapons;
        final PluginConfig.RollCommandSettings rollSettings = settings.rollCommand;
        final PluginConfig.MessagesConfig messages = config.messages;

        if (!rollSettings.enabled) {
            MessageUtil.send(player, messages.rollDisabledMessage);
            return;
        }

        if (rollSettings.permission != null && !rollSettings.permission.isEmpty() && !player.hasPermission(rollSettings.permission)) {
            MessageUtil.send(player, "<red>You do not have permission to run this command!</red>");
            return;
        }

        final StrengthService strengthService = plugin.getStrengthService();
        final String assigned = strengthService.getAssignedWeapon(player);

        if (assigned != null) {
            final String formattedWeapon = ItemResolver.resolveWeaponDisplayName(assigned, settings.weaponCustomMessages);
            MessageUtil.send(
                player,
                messages.alreadyAssignedMessage,
                "weapon", formattedWeapon
            );
            return;
        }

        // Cooldown check
        if (rollSettings.cooldownSeconds > 0) {
            final long now = System.currentTimeMillis();
            final Long lastUsed = cooldowns.get(player.getUniqueId());
            if (lastUsed != null) {
                final long elapsedSeconds = (now - lastUsed) / 1000L;
                if (elapsedSeconds < rollSettings.cooldownSeconds) {
                    final long remaining = rollSettings.cooldownSeconds - elapsedSeconds;
                    MessageUtil.send(
                        player,
                        "<red>You must wait <seconds>s before using /weaponclass roll again!</red>",
                        "seconds", String.valueOf(remaining)
                    );
                    return;
                }
            }
            cooldowns.put(player.getUniqueId(), now);
        }

        // Check if PvPManager is active and player is in combat
        if (config.pvpmanager.enabled && config.pvpmanager.preventRerollInCombat) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("PvPManager")) {
                if (lunatech.strength.integration.PvPManagerHook.isInCombat(plugin, player)) {
                    MessageUtil.send(player, messages.cannotRerollInCombatMessage);
                    return;
                }
            }
        }

        // Check WorldGuard region restrictions
        if (config.worldguard.enabled && config.worldguard.preventRerollInSafezone) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
                if (!lunatech.strength.integration.WorldGuardHook.isRerollAllowed(plugin, player, player.getLocation())) {
                    MessageUtil.send(player, messages.cannotRerollInRegionMessage);
                    return;
                }
            }
        }

        new WeaponRollTask(plugin, player).start();
    }
}
