package lunatech.strength.hook.pvpmanager;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.hook.AbstractHook;
import lunatech.strength.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Isolated hook for PvPManager implementing AbstractHook lifecycle logging and combat tag checks.
 */
public final class PvPManagerHook extends AbstractHook {

    public PvPManagerHook(@NotNull Strength plugin) {
        super(plugin);
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginEnabled(Hook.PvPManager.getPluginName());
    }

    @Override
    public void onLoad(@NotNull AbstractStrength plugin) {
        // Lifecycle load logic if needed
    }

    /**
     * Checks if the player is currently tagged in combat according to PvPManager and plugin configuration.
     *
     * @param plugin the plugin instance
     * @param player the player to check
     * @return true if player is in combat, false otherwise
     */
    public static boolean isInCombat(@NotNull Strength plugin, @NotNull Player player) {
        if (!plugin.getConfigHandler().getConfig().pvpmanager.enabled) {
            return false;
        }
        return isTaggedInCombat(player);
    }

    /**
     * Direct check if player is currently tagged in combat according to PvPManager.
     *
     * @param player the player to check
     * @return true if player is in combat, false otherwise
     */
    public static boolean isTaggedInCombat(@NotNull Player player) {
        if (!Bukkit.getPluginManager().isPluginEnabled("PvPManager")) {
            return false;
        }
        try {
            final me.chancesd.pvpmanager.player.CombatPlayer pvpPlayer = me.chancesd.pvpmanager.PvPManager.getInstance().getPlayerManager().get(player);
            return pvpPlayer != null && pvpPlayer.isInCombat();
        } catch (Throwable ignored) {
            return false;
        }
    }
}
