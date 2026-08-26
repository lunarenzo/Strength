package lunatech.strength.integration;

import lunatech.strength.Strength;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Isolated hook for PvPManager to prevent ClassNotFoundException when PvPManager is uninstalled.
 */
public final class PvPManagerHook {
    private PvPManagerHook() {
    }

    /**
     * Checks if the player is currently tagged in combat according to PvPManager.
     *
     * @param plugin the plugin instance
     * @param player the player to check
     * @return true if player is in combat, false otherwise
     */
    public static boolean isInCombat(@NotNull Strength plugin, @NotNull Player player) {
        if (!plugin.getConfigHandler().getConfig().pvpmanager.enabled || !plugin.getConfigHandler().getConfig().pvpmanager.preventRerollInCombat) {
            return false;
        }
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
