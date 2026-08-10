package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.Reloadable;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    public static final String BASE_PERM = "strength.command";
    private final Strength plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(Strength plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AbstractStrength plugin) {
        CommandAPI.onLoad(
            new CommandAPIPaperConfig(plugin)
                .silentLogs(true)
        );
    }

    @Override
    public void onEnable(AbstractStrength plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onEnable();

        // Register commands here
        new StrengthCommand(plugin)
            .command()
            .withAliases()
            .register();
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onDisable();
    }
}