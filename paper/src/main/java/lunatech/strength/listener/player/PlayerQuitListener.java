package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.service.StrengthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that handles weapon clearing when clear-on-quit is enabled.
 */
public final class PlayerQuitListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public PlayerQuitListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        if (plugin.getConfigHandler().getConfig().weapons.clearOnQuit) {
            final String assigned = strengthService.getAssignedWeapon(player);
            if (assigned != null) {
                strengthService.setAssignedWeapon(player, null);
            }
        }
    }
}
