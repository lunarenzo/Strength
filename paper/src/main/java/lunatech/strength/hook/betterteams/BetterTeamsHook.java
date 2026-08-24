package lunatech.strength.hook.betterteams;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.hook.AbstractHook;
import lunatech.strength.hook.Hook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * High-throughput, reflection-based soft dependency hook for BetterTeams by booksaw.
 * Handles team and ally friendly fire protection checks across weapon abilities.
 */
public final class BetterTeamsHook extends AbstractHook {
    private static Method getTeamMethod;
    private static Method canDamageMethod;

    public BetterTeamsHook(@NotNull Strength plugin) {
        super(plugin);
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginEnabled(Hook.BetterTeams.getPluginName());
    }

    @Override
    public void onLoad(@NotNull AbstractStrength plugin) {
        if (!isHookLoaded()) {
            return;
        }

        try {
            final Class<?> teamClass = Class.forName("com.booksaw.betterTeams.Team");
            getTeamMethod = teamClass.getMethod("getTeam", Player.class);
            canDamageMethod = teamClass.getMethod("canDamage", Player.class, Player.class);
        } catch (Throwable ignored) {
        }
    }

    /**
     * Checks if the attacker is allowed to damage the victim based on BetterTeams rules.
     * Returns true if BetterTeams is disabled/missing, or if damage is permitted.
     *
     * @param attacker the attacking player
     * @param victim   the target entity
     * @return true if damage is allowed, false if friendly fire / ally protection prevents it
     */
    public static boolean canDamage(@NotNull Player attacker, @Nullable Entity victim) {
        if (!(victim instanceof Player target)) {
            return true;
        }

        if (attacker.getUniqueId().equals(target.getUniqueId())) {
            return false;
        }

        if (!Bukkit.getPluginManager().isPluginEnabled("BetterTeams")) {
            return true;
        }

        try {
            if (getTeamMethod == null || canDamageMethod == null) {
                final Class<?> teamClass = Class.forName("com.booksaw.betterTeams.Team");
                getTeamMethod = teamClass.getMethod("getTeam", Player.class);
                canDamageMethod = teamClass.getMethod("canDamage", Player.class, Player.class);
            }

            final Object attackerTeam = getTeamMethod.invoke(null, attacker);
            if (attackerTeam == null) {
                return true;
            }

            final Object allowed = canDamageMethod.invoke(attackerTeam, target, attacker);
            return allowed instanceof Boolean b ? b : true;
        } catch (Throwable ignored) {
            return true;
        }
    }
}
