package lunatech.strength.listener.player;

import lunatech.strength.service.StrengthService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that synchronizes strength attributes on player join.
 */
public final class PlayerJoinListener implements Listener {
    private final StrengthService strengthService;

    public PlayerJoinListener(@NotNull StrengthService strengthService) {
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final int strength = strengthService.getStrength(player);
        
        // Re-apply modifier on join to synchronize entity state
        strengthService.applyAttributeModifier(player, strength);
    }
}
