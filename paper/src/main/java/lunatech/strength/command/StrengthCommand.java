package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
            .withHelp("Base command.", "Base command.")
            .withPermission(BASE_PERM)
            .withSubcommands(
                new TranslationCommand().command(),
                new DumpCommand().command(),
                new CommandAPICommand("withdraw")
                    .withHelp("Withdraw strength into a physical item.", "Withdraw strength into a physical item.")
                    .withArguments(new IntegerArgument("amount", 1))
                    .executesPlayer(this::executorWithdraw),
                new CommandAPICommand("reload")
                    .withHelp("Reload the plugin configuration and translations.", "Reload the plugin configuration and translations.")
                    .withPermission(BASE_PERM + ".reload")
                    .executes(this::executorReload)
            )
            .executes(this::executorStrength);
    }

    private void executorStrength(CommandSender sender, CommandArguments args) {
        final StrengthService strengthService = plugin.getStrengthService();
        if (sender instanceof Player player) {
            final int strength = strengthService.getStrength(player);
            player.sendMessage(
                ColorParser.of("<white>Your current strength level is: <gold><strength></gold>.")
                    .with("strength", String.valueOf(strength))
                    .build()
            );
        } else {
            sender.sendMessage(
                ColorParser.of("<white>Read more about CommandAPI &9<click:open_url:'https://commandapi.jorel.dev/9.0.3/'>here</click><white>.")
                    .legacy()
                    .build()
            );
        }
    }

    private void executorWithdraw(Player player, CommandArguments args) {
        final int amount = (int) args.get("amount");
        final StrengthService strengthService = plugin.getStrengthService();
        final int currentStrength = strengthService.getStrength(player);
        final int minStrength = plugin.getConfigHandler().getConfig().strength.minStrength;

        if (currentStrength - amount < minStrength) {
            player.sendMessage(
                ColorParser.of("<red>You do not have enough strength to withdraw <amount>! (Minimum required to keep: <min>, Current: <current>)</red>")
                    .with("amount", String.valueOf(amount))
                    .with("min", String.valueOf(minStrength))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            player.sendMessage(
                ColorParser.of("<red>Your inventory is full!</red>")
                    .build()
            );
            return;
        }

        // Deduct strength from base
        strengthService.setStrength(player, currentStrength - amount);

        // Give physical item
        final ItemStack strengthItem = strengthService.createStrengthItem(amount);
        player.getInventory().addItem(strengthItem);

        player.sendMessage(
            ColorParser.of("<green>Successfully withdrew <amount> Strength into a physical item!</green>")
                .with("amount", String.valueOf(amount))
                .build()
        );
    }

    private void executorReload(CommandSender sender, CommandArguments args) {
        plugin.getConfigHandler().onLoad(plugin);
        io.github.milkdrinkers.wordweaver.Translation.reload();
        sender.sendMessage(
            ColorParser.of("<green>Successfully reloaded plugin configuration and translations!</green>")
                .build()
        );
    }
}
