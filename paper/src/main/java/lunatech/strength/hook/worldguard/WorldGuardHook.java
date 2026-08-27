package lunatech.strength.hook.worldguard;

import lunatech.strength.Strength;
import lunatech.strength.hook.AbstractHook;
import lunatech.strength.hook.Hook;

/**
 * A hook to interface with WorldGuard plugin status and lifecycle logging.
 */
public class WorldGuardHook extends AbstractHook {

    /**
     * Instantiates a new WorldGuard hook.
     *
     * @param plugin the plugin instance
     */
    public WorldGuardHook(Strength plugin) {
        super(plugin);
    }

    /**
     * Check if WorldGuard is present on the server.
     *
     * @return true if WorldGuard plugin is enabled
     */
    @Override
    public boolean isHookLoaded() {
        return isPluginEnabled(Hook.WorldGuard.getPluginName());
    }
}
