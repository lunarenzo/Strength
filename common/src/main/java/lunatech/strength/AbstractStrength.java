package lunatech.strength;

import lunatech.strength.config.ConfigHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractStrength extends JavaPlugin {
    private static AbstractStrength instance;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static AbstractStrength getInstance() {
        return AbstractStrength.instance;
    }

    AbstractStrength() {
        AbstractStrength.instance = this;
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    public abstract @NotNull ConfigHandler getConfigHandler();

    /**
     * Gets strength service.
     *
     * @return the strength service
     */
    public abstract @NotNull lunatech.strength.service.StrengthService getStrengthService();
}
