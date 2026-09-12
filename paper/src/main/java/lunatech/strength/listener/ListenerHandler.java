package lunatech.strength.listener;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import lunatech.strength.Strength;
import lunatech.strength.listener.player.ArmorsAbilityListener;
import lunatech.strength.listener.player.AxeAbilityListener;
import lunatech.strength.listener.player.BowAbilityListener;
import lunatech.strength.listener.player.CrossbowAbilityListener;
import lunatech.strength.listener.player.Crossbow2AbilityListener;
import lunatech.strength.listener.player.EnchantmentRestrictionListener;
import lunatech.strength.listener.player.MaceAbilityListener;
import lunatech.strength.listener.player.MaceListener;
import lunatech.strength.listener.player.PlayerJoinListener;
import lunatech.strength.listener.player.PlayerKillListener;
import lunatech.strength.listener.player.PlayerQuitListener;
import lunatech.strength.listener.player.PotionListener;
import lunatech.strength.listener.player.RerollConfirmationGuiListener;
import lunatech.strength.listener.player.RerollConsumeListener;
import lunatech.strength.listener.player.RerollRecipeGuiListener;
import lunatech.strength.listener.player.ShieldAbilityListener;
import lunatech.strength.listener.player.SpearAbilityListener;
import lunatech.strength.listener.player.StrengthConsumeListener;
import lunatech.strength.listener.player.StrengthRecipeGuiListener;
import lunatech.strength.listener.player.SwordAbilityListener;
import lunatech.strength.listener.player.TotemRuleListener;
import lunatech.strength.listener.player.Trident2AbilityListener;
import lunatech.strength.listener.player.TridentAbilityListener;
import lunatech.strength.listener.player.WeaponsGuiListener;
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
        
        listeners.add(new PlayerKillListener(this.plugin, this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new PlayerJoinListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new PlayerQuitListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new StrengthConsumeListener(this.plugin.getStrengthService(), this.plugin.getConfigHandler()));
        listeners.add(new RerollConsumeListener(this.plugin));
        listeners.add(new RerollConfirmationGuiListener(this.plugin));
        listeners.add(new WeaponsGuiListener(this.plugin));
        listeners.add(new RerollRecipeGuiListener());
        listeners.add(new StrengthRecipeGuiListener());
        listeners.add(new TridentAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new Trident2AbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new BowAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new ShieldAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new CrossbowAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new Crossbow2AbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new SwordAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new AxeAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new MaceAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new ArmorsAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new SpearAbilityListener(this.plugin, this.plugin.getStrengthService()));
        listeners.add(new MaceListener(this.plugin));
        listeners.add(new PotionListener(this.plugin));
        listeners.add(new EnchantmentRestrictionListener(this.plugin));
        listeners.add(new TotemRuleListener(this.plugin));

        if (plugin.getServer().getPluginManager().isPluginEnabled("AuthMe")) {
            try {
                final Class<?> clazz = Class.forName("lunatech.strength.listener.plugin.AuthMeListener");
                final Listener listener = (Listener) clazz.getDeclaredConstructor(Strength.class).newInstance(this.plugin);
                listeners.add(listener);
            } catch (Throwable ignored) {
            }
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("PvPManager")) {
            try {
                final Class<?> clazz = Class.forName("lunatech.strength.listener.plugin.PvPManagerListener");
                final Listener listener = (Listener) clazz.getDeclaredConstructor(Strength.class).newInstance(this.plugin);
                listeners.add(listener);
            } catch (Throwable ignored) {
            }
        }

        // Register listeners here
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
        BowAbilityListener.cleanupActiveCobwebs();
    }
}
