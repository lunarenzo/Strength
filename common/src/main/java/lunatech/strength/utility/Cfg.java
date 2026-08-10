package lunatech.strength.utility;

import lunatech.strength.AbstractStrength;
import lunatech.strength.config.ConfigHandler;
import lunatech.strength.config.PluginConfig;
import org.jetbrains.annotations.NotNull;

/**
 * Convenience class for accessing {@link ConfigHandler#getConfig}
 */
public final class Cfg {
    /**
     * Convenience method for {@link ConfigHandler#getConfig} to getConnection {@link PluginConfig}
     *
     * @return the config
     */
    @NotNull
    public static PluginConfig get() {
        return AbstractStrength.getInstance().getConfigHandler().getConfig();
    }
}
