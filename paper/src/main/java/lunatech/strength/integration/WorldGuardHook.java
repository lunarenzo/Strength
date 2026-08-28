package lunatech.strength.integration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lunatech.strength.Strength;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Modern WorldGuard 7.x integration handler for region flags & protection queries.
 */
public final class WorldGuardHook {
    public static StateFlag STRENGTH_PVP_LOSS;
    public static StateFlag STRENGTH_WEAPON_ABILITIES;
    public static StateFlag STRENGTH_REROLL;

    private WorldGuardHook() {
    }

    /**
     * Registers custom WorldGuard flags upon plugin loading.
     */
    public static void registerFlags() {
        try {
            final FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();

            STRENGTH_PVP_LOSS = registerStateFlag(registry, "strength-pvp-loss", true);
            STRENGTH_WEAPON_ABILITIES = registerStateFlag(registry, "strength-weapon-abilities", true);
            STRENGTH_REROLL = registerStateFlag(registry, "strength-reroll", true);
        } catch (Throwable ignored) {
        }
    }

    private static StateFlag registerStateFlag(FlagRegistry registry, String name, boolean defaultValue) {
        try {
            StateFlag flag = new StateFlag(name, defaultValue);
            registry.register(flag);
            return flag;
        } catch (Throwable e) {
            try {
                com.sk89q.worldguard.protection.flags.Flag<?> existing = registry.get(name);
                if (existing instanceof StateFlag stateFlag) {
                    return stateFlag;
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    public static boolean isAbilityAllowed(@NotNull Strength plugin, @NotNull Player player, @NotNull Location loc) {
        if (!plugin.getConfigHandler().getConfig().worldguard.enabled || !plugin.getConfigHandler().getConfig().worldguard.preventAbilitiesInSafezone) {
            return true;
        }
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            return true;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(loc);
            com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (query.queryState(weLoc, localPlayer, Flags.PVP) == StateFlag.State.DENY) {
                return false;
            }
            if (STRENGTH_WEAPON_ABILITIES != null && query.queryState(weLoc, localPlayer, STRENGTH_WEAPON_ABILITIES) == StateFlag.State.DENY) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    public static boolean isPvPLossAllowed(@NotNull Strength plugin, @Nullable Player victim, @NotNull Location loc) {
        if (!plugin.getConfigHandler().getConfig().worldguard.enabled || !plugin.getConfigHandler().getConfig().worldguard.preventStrengthLossInSafezone) {
            return true;
        }
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            return true;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(loc);
            com.sk89q.worldguard.LocalPlayer localPlayer = victim != null ? WorldGuardPlugin.inst().wrapPlayer(victim) : null;

            if (STRENGTH_PVP_LOSS != null && query.queryState(weLoc, localPlayer, STRENGTH_PVP_LOSS) == StateFlag.State.DENY) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }

    public static boolean isRerollAllowed(@NotNull Strength plugin, @NotNull Player player, @NotNull Location loc) {
        if (!plugin.getConfigHandler().getConfig().worldguard.enabled || !plugin.getConfigHandler().getConfig().worldguard.preventRerollInSafezone) {
            return true;
        }
        if (!plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            return true;
        }
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(loc);
            com.sk89q.worldguard.LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

            if (STRENGTH_REROLL != null && query.queryState(weLoc, localPlayer, STRENGTH_REROLL) == StateFlag.State.DENY) {
                return false;
            }
        } catch (Throwable ignored) {
        }
        return true;
    }
}
