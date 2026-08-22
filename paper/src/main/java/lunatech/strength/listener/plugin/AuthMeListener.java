package lunatech.strength.listener.plugin;

import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.RegisterEvent;
import lunatech.strength.Strength;
import lunatech.strength.task.WeaponRollTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener for AuthMe authentication & registration events.
 * Ensures first-time or unassigned players only trigger the weapon roll assignment AFTER successful authentication.
 */
public final class AuthMeListener implements Listener {
    private final Strength plugin;

    public AuthMeListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAuthMeLogin(@NotNull LoginEvent event) {
        handleAuthSuccess(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAuthMeRegister(@NotNull RegisterEvent event) {
        handleAuthSuccess(event.getPlayer());
    }

    private void handleAuthSuccess(@NotNull Player player) {
        if (!plugin.getConfigHandler().getConfig().authme.enabled) {
            return;
        }

        final String assigned = plugin.getStrengthService().getAssignedWeapon(player);
        if (assigned == null) {
            final List<String> available = plugin.getConfigHandler().getConfig().weapons.availableWeapons;
            if (available != null && !available.isEmpty()) {
                final int delaySeconds = plugin.getConfigHandler().getConfig().weapons.rollDelaySeconds;
                
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        new WeaponRollTask(plugin, player).start();
                    }
                }, Math.max(0L, delaySeconds * 20L));
            }
        }
    }
}
