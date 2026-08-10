package lunatech.strength.cooldown.listener;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to handle registration of event listeners.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ListenerHandler implements Reloadable {
    private final AbstractStrength plugin;
    private final List<Listener> listeners = new ArrayList<>();

    public ListenerHandler(AbstractStrength plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(AbstractStrength plugin) {
    }

    @Override
    public void onEnable(AbstractStrength plugin) {
        listeners.clear();
        listeners.add(new CooldownListener(plugin));

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
    }
}
