package lunatech.strength.listener;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
public class ListenerHandler implements Reloadable {
    private final Strength plugin;
    private final List<Listener> listeners = new ArrayList<>();

    /**
     * Instantiates a the Listener handler.
     *
     * @param plugin the plugin instance
     */
    public ListenerHandler(Strength plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(AbstractStrength plugin) {
        listeners.clear(); // Clear the list to avoid duplicate listeners when reloading the plugin
        
        listeners.add(new lunatech.strength.listener.player.PlayerKillListener(this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new lunatech.strength.listener.player.PlayerJoinListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.StrengthConsumeListener(this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.TridentAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.BowAbilityListener(this.plugin, this.plugin.getStrengthService()));

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}
