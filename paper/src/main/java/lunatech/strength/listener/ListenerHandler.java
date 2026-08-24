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
        
        listeners.add(new lunatech.strength.listener.player.PlayerKillListener(this.plugin, this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new lunatech.strength.listener.player.PlayerJoinListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.StrengthConsumeListener(this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new lunatech.strength.listener.player.RerollConsumeListener(this.plugin));
        listeners.add(new lunatech.strength.listener.player.RerollConfirmationGuiListener(this.plugin));
        listeners.add(new lunatech.strength.listener.player.WeaponsGuiListener(this.plugin));
        listeners.add(new lunatech.strength.listener.player.RerollRecipeGuiListener());
        listeners.add(new lunatech.strength.listener.player.TridentAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.BowAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.ShieldAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.CrossbowAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.SwordAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new lunatech.strength.listener.player.AxeAbilityListener(this.plugin, this.plugin.getStrengthService()));

        if (plugin.getServer().getPluginManager().isPluginEnabled("AuthMe")) {
            listeners.add(new lunatech.strength.listener.plugin.AuthMeListener(this.plugin));
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PvPManager")) {
            listeners.add(new lunatech.strength.listener.plugin.PvPManagerListener(this.plugin));
        }

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
        lunatech.strength.listener.player.BowAbilityListener.cleanupActiveCobwebs();
    }
}
