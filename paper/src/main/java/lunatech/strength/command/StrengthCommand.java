package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.command.CommandSender;

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
                new DumpCommand().command()
            )
            .executes(this::executorStrength);
    }

    private void executorStrength(CommandSender sender, CommandArguments args) {
        sender.sendMessage(
            ColorParser.of("<white>Read more about CommandAPI &9<click:open_url:'https://commandapi.jorel.dev/9.0.3/'>here</click><white>.")
                .legacy() // Parse legacy color codes
                .build()
        );
    }
}
