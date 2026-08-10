package io.github.exampleuser.example;

import io.github.exampleuser.example.api.ExampleAPI;
import io.github.exampleuser.example.command.CommandHandler;
import io.github.exampleuser.example.config.ConfigHandler;
import io.github.exampleuser.example.cooldown.CooldownHandler;
import io.github.exampleuser.example.hook.HookManager;
import io.github.exampleuser.example.listener.ListenerHandler;
import io.github.exampleuser.example.threadutil.SchedulerHandler;
import io.github.exampleuser.example.translation.TranslationHandler;
import io.github.exampleuser.example.updatechecker.UpdateHandler;
import io.github.exampleuser.example.utility.Logger;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Example extends AbstractExample {
    private static Example instance;

    private ConfigHandler configHandler;
    private TranslationHandler translationHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateHandler updateHandler;
    private SchedulerHandler schedulerHandler;
    private CooldownHandler cooldownHandler;
    private ExampleAPIProvider apiHandler;
    private io.github.exampleuser.example.service.StrengthService strengthService;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    @Override
    public void onLoad() {
        instance = this;

        configHandler = new ConfigHandler(this);
        translationHandler = new TranslationHandler(configHandler);

        // Initialize Strength Service
        strengthService = new io.github.exampleuser.example.service.impl.DefaultStrengthService(
            new io.github.exampleuser.example.data.repository.impl.PDCPlayerRepository(),
            configHandler
        );

        hookManager = new HookManager(this);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        updateHandler = new UpdateHandler(this);
        schedulerHandler = new SchedulerHandler();
        cooldownHandler = new CooldownHandler();
        apiHandler = new ExampleAPIProvider(this);

        handlers = List.of(
            configHandler,
            translationHandler,
            hookManager,
            commandHandler,
            listenerHandler,
            updateHandler,
            schedulerHandler,
            cooldownHandler,
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

    public @NotNull ExampleAPI getApiHandler() {
        return apiHandler;
    }

    public @NotNull io.github.exampleuser.example.service.StrengthService getStrengthService() {
        return strengthService;
    }
}
