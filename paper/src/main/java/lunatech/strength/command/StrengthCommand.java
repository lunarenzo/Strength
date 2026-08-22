package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.config.PluginConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

import static lunatech.strength.command.CommandHandler.BASE_PERM;

/**
 * Class containing the code for the strength command.
 */
final class StrengthCommand extends Command {
    private final AbstractStrength plugin;

    /**
     * Instantiates and registers a new command.
     */
    StrengthCommand(AbstractStrength plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("strength")
            .withHelp("Base strength command.", "Base strength command.")
            .withPermission(BASE_PERM)
            .withSubcommands(
                new CommandAPICommand("info")
                    .withHelp("Check your current strength and assigned weapon.", "Check your current strength and assigned weapon.")
                    .withPermission(BASE_PERM)
                    .executes(this::executorStrength),
                new CommandAPICommand("withdraw")
                    .withHelp("Withdraw strength into a physical item.", "Withdraw strength into a physical item.")
                    .withArguments(new IntegerArgument("amount", 1))
                    .executesPlayer(this::executorWithdraw),
                new CommandAPICommand("give")
                    .withHelp("Give strength items or reroll items to players.", "Give strength items or reroll items to players.")
                    .withPermission(BASE_PERM + ".give")
                    .withSubcommands(
                        new CommandAPICommand("strengthitem")
                            .withHelp("Give physical strength item to a player.", "Give physical strength item to a player.")
                            .withArguments(
                                new EntitySelectorArgument.OnePlayer("target"),
                                new IntegerArgument("amount", 1),
                                new IntegerArgument("strength_amount", 1)
                            )
                            .executes(this::executorGiveStrengthItem),
                        new CommandAPICommand("rollitem")
                            .withHelp("Give reroll item to a player.", "Give reroll item to a player.")
                            .withArguments(
                                new EntitySelectorArgument.OnePlayer("target"),
                                new IntegerArgument("amount", 1)
                            )
                            .executes(this::executorGiveRollItem),
                        new CommandAPICommand("rerollitem")
                            .withHelp("Give reroll item to a player.", "Give reroll item to a player.")
                            .withArguments(
                                new EntitySelectorArgument.OnePlayer("target"),
                                new IntegerArgument("amount", 1)
                            )
                            .executes(this::executorGiveRollItem)
                    ),
                new CommandAPICommand("changeweapon")
                    .withHelp("Change a target player's assigned weapon.", "Change a target player's assigned weapon.")
                    .withPermission(BASE_PERM + ".changeweapon")
                    .withArguments(
                        new EntitySelectorArgument.OnePlayer("target"),
                        new StringArgument("weapon").replaceSuggestions(
                            ArgumentSuggestions.stringCollection(info ->
                                plugin.getConfigHandler().getConfig().weapons.availableWeapons
                                    .stream()
                                    .map(String::toLowerCase)
                                    .toList()
                            )
                        )
                    )
                    .executes(this::executorChangeWeapon),
                new CommandAPICommand("set")
                    .withHelp("Set a target player's strength level.", "Set a target player's strength level.")
                    .withPermission(BASE_PERM + ".set")
                    .withArguments(
                        new EntitySelectorArgument.OnePlayer("target"),
                        new IntegerArgument("amount", 0)
                    )
                    .executes(this::executorSetStrength),
                new CommandAPICommand("reload")
                    .withHelp("Reload the plugin configuration and translations.", "Reload the plugin configuration and translations.")
                    .withPermission(BASE_PERM + ".reload")
                    .executes(this::executorReload)
            )
            .executes(this::executorStrength);
    }

    private void executorStrength(CommandSender sender, CommandArguments args) {
        final StrengthService strengthService = plugin.getStrengthService();
        final PluginConfig config = plugin.getConfigHandler().getConfig();
        final PluginConfig.MessagesConfig messages = config.messages;

        if (sender instanceof Player player) {
            final int strength = strengthService.getStrength(player);
            final String assignedRaw = strengthService.getAssignedWeapon(player);

            String weaponDisplay = messages.unassignedWeaponMessage;
            if (assignedRaw != null && !assignedRaw.isEmpty()) {
                final Map<String, String> customMap = config.weapons.weaponCustomMessages;
                if (customMap != null && customMap.containsKey(assignedRaw)) {
                    weaponDisplay = customMap.get(assignedRaw);
                } else {
                    weaponDisplay = assignedRaw.toUpperCase();
                }
            }

            lunatech.strength.utility.MessageUtil.send(
                player,
                messages.strengthCheckMessage,
                Map.of("strength", String.valueOf(strength), "weapon", weaponDisplay)
            );
        } else {
            sender.sendMessage(
                ColorParser.of("<white>Read more about CommandAPI &9<click:open_url:'https://commandapi.jorel.dev/9.0.3/'>here</click><white>.")
                    .legacy()
                    .build()
            );
        }
    }

    private void executorGiveStrengthItem(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final int amount = (int) args.get("amount");
        final int strengthAmount = (int) args.get("strength_amount");
        final PluginConfig.MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (target == null) {
            lunatech.strength.utility.MessageUtil.send(sender, messages.targetNotFoundMessage);
            return;
        }

        final ItemStack item = plugin.getStrengthService().createStrengthItem(strengthAmount);
        item.setAmount(amount);

        final Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }

        lunatech.strength.utility.MessageUtil.send(
            sender,
            messages.giveStrengthItemSuccessSenderMessage,
            Map.of(
                "target", target.getName(),
                "amount", String.valueOf(amount),
                "value", String.valueOf(strengthAmount)
            )
        );

        if (!target.equals(sender)) {
            lunatech.strength.utility.MessageUtil.send(
                target,
                messages.giveStrengthItemSuccessTargetMessage,
                Map.of(
                    "amount", String.valueOf(amount),
                    "value", String.valueOf(strengthAmount)
                )
            );
        }
    }

    private void executorGiveRollItem(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final int amount = (int) args.get("amount");
        final PluginConfig.MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (target == null) {
            lunatech.strength.utility.MessageUtil.send(sender, messages.targetNotFoundMessage);
            return;
        }

        final ItemStack item = plugin.getStrengthService().createRerollItem();
        item.setAmount(amount);

        final Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }

        lunatech.strength.utility.MessageUtil.send(
            sender,
            messages.giveRollItemSuccessSenderMessage,
            Map.of(
                "target", target.getName(),
                "amount", String.valueOf(amount)
            )
        );

        if (!target.equals(sender)) {
            lunatech.strength.utility.MessageUtil.send(
                target,
                messages.giveRollItemSuccessTargetMessage,
                "amount", String.valueOf(amount)
            );
        }
    }

    private void executorWithdraw(Player player, CommandArguments args) {
        final int amount = (int) args.get("amount");
        final StrengthService strengthService = plugin.getStrengthService();
        final int currentStrength = strengthService.getStrength(player);
        final int minStrength = plugin.getConfigHandler().getConfig().strength.minStrength;
        final PluginConfig.MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (currentStrength - amount < minStrength) {
            lunatech.strength.utility.MessageUtil.send(
                player,
                messages.withdrawNotEnoughMessage,
                Map.of("amount", String.valueOf(amount), "min", String.valueOf(minStrength), "current", String.valueOf(currentStrength))
            );
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            lunatech.strength.utility.MessageUtil.send(
                player,
                messages.withdrawFullInventoryMessage
            );
            return;
        }

        // Deduct strength from base
        strengthService.setStrength(player, currentStrength - amount);

        // Give physical item
        final ItemStack strengthItem = strengthService.createStrengthItem(amount);
        player.getInventory().addItem(strengthItem);

        lunatech.strength.utility.MessageUtil.send(
            player,
            messages.withdrawSuccessMessage,
            "amount", String.valueOf(amount)
        );
    }

    private void executorChangeWeapon(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final String weapon = ((String) args.get("weapon")).toLowerCase();
        final List<String> availableWeapons = plugin.getConfigHandler().getConfig().weapons.availableWeapons
            .stream()
            .map(String::toLowerCase)
            .toList();
        final PluginConfig.MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (target == null) {
            lunatech.strength.utility.MessageUtil.send(sender, messages.targetNotFoundMessage);
            return;
        }

        if (!availableWeapons.contains(weapon)) {
            lunatech.strength.utility.MessageUtil.send(
                sender,
                messages.changeWeaponInvalidMessage,
                "list", String.join(", ", availableWeapons)
            );
            return;
        }

        final StrengthService strengthService = plugin.getStrengthService();
        strengthService.setAssignedWeapon(target, weapon);

        lunatech.strength.utility.MessageUtil.send(
            sender,
            messages.changeWeaponSuccessSenderMessage,
            Map.of("target", target.getName(), "weapon", weapon.toUpperCase())
        );

        lunatech.strength.utility.MessageUtil.send(
            target,
            messages.changeWeaponSuccessTargetMessage,
            "weapon", weapon.toUpperCase()
        );
    }

    private void executorSetStrength(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final int amount = (int) args.get("amount");
        final PluginConfig.MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (target == null) {
            lunatech.strength.utility.MessageUtil.send(sender, messages.targetNotFoundMessage);
            return;
        }

        final StrengthService strengthService = plugin.getStrengthService();
        final int oldStrength = strengthService.getStrength(target);
        strengthService.setStrength(target, amount);

        lunatech.strength.utility.MessageUtil.send(
            sender,
            messages.setStrengthSuccessMessage,
            Map.of("target", target.getName(), "old", String.valueOf(oldStrength), "amount", String.valueOf(amount))
        );
    }

    private void executorReload(CommandSender sender, CommandArguments args) {
        plugin.getConfigHandler().onLoad(plugin);
        sender.sendMessage(
            ColorParser.of("<green>Successfully reloaded plugin configuration!</green>")
                .build()
        );
    }
}
