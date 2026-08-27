package lunatech.strength;

import lunatech.strength.api.StrengthAPI;
import lunatech.strength.command.CommandHandler;
import lunatech.strength.config.ConfigHandler;
import lunatech.strength.cooldown.CooldownHandler;
import lunatech.strength.hook.HookManager;
import lunatech.strength.listener.ListenerHandler;
import lunatech.strength.threadutil.SchedulerHandler;
import lunatech.strength.updatechecker.UpdateHandler;
import lunatech.strength.utility.Logger;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Strength extends AbstractStrength {
    private static Strength instance;

    private ConfigHandler configHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateHandler updateHandler;
    private SchedulerHandler schedulerHandler;
    private CooldownHandler cooldownHandler;
    private lunatech.strength.recipe.RecipeHandler recipeHandler;
    private StrengthAPIProvider apiHandler;
    private lunatech.strength.service.StrengthService strengthService;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    @Override
    public void onLoad() {
        instance = this;

        configHandler = new ConfigHandler(this);

        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            try {
                lunatech.strength.integration.WorldGuardHook.registerFlags();
            } catch (Throwable ignored) {
            }
        }

        // Initialize Strength Service
        strengthService = new lunatech.strength.service.impl.DefaultStrengthService(
            new lunatech.strength.data.repository.impl.PDCPlayerRepository(),
            configHandler
        );

        hookManager = new HookManager(this);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        updateHandler = new UpdateHandler(this);
        schedulerHandler = new SchedulerHandler();
        cooldownHandler = new CooldownHandler();
        recipeHandler = new lunatech.strength.recipe.RecipeHandler(this);
        apiHandler = new StrengthAPIProvider(this);

        handlers = List.of(
            configHandler,
            hookManager,
            commandHandler,
            listenerHandler,
            updateHandler,
            schedulerHandler,
            cooldownHandler,
            recipeHandler,
            apiHandler
        );

        for (Reloadable handler : handlers)
            handler.onLoad(instance);
    }

    @Override
    public void onEnable() {
        for (Reloadable handler : handlers)
            handler.onEnable(instance);
    }

    @Override
    public void onDisable() {
        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }

    /**
     * Use to reload the entire plugin.
     */
    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }

    @Override
    public @NotNull ConfigHandler getConfigHandler() {
        return configHandler;
    }

    public @NotNull HookManager getHookManager() {
        return hookManager;
    }

    public @NotNull UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public @NotNull StrengthAPI getApiHandler() {
        return apiHandler;
    }

    public @NotNull lunatech.strength.service.StrengthService getStrengthService() {
        return strengthService;
    }
}
