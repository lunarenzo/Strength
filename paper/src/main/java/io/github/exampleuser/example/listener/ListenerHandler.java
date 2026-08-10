package io.github.exampleuser.example.listener;

import io.github.exampleuser.example.AbstractExample;
import io.github.exampleuser.example.Example;
import io.github.exampleuser.example.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final Example plugin;
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(Example plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(AbstractExample plugin) {
        listeners.clear(); // Clear the list to avoid duplicate listeners when reloading the plugin
        
        listeners.add(new io.github.exampleuser.example.listener.player.PlayerKillListener(this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new io.github.exampleuser.example.listener.player.PlayerJoinListener(this.plugin.getStrengthService()));

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}
