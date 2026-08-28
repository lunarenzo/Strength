package lunatech.strength.integration;

import lunatech.strength.Strength;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Legacy/compatibility wrapper for PvPManager hook.
 */
public final class PvPManagerHook {
    private PvPManagerHook() {
    }

    public static boolean isInCombat(@NotNull Strength plugin, @NotNull Player player) {
        return lunatech.strength.hook.pvpmanager.PvPManagerHook.isInCombat(plugin, player);
    }

    public static boolean isTaggedInCombat(@NotNull Player player) {
        return lunatech.strength.hook.pvpmanager.PvPManagerHook.isTaggedInCombat(player);
    }
}
