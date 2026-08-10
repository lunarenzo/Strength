package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener that synchronizes strength attributes and handles initial weapon rolls on player join.
 */
public final class PlayerJoinListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public PlayerJoinListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final int strength = strengthService.getStrength(player);
        
        // Re-apply modifier on join to synchronize entity state
        strengthService.applyAttributeModifier(player, strength);

        // Check if player has an assigned weapon, if not trigger the rolling process
        final String assignedWeapon = strengthService.getAssignedWeapon(player);
        if (assignedWeapon == null) {
            final List<String> available = plugin.getConfigHandler().getConfig().weapons.availableWeapons;
            if (available != null && !available.isEmpty()) {
                final int delaySeconds = plugin.getConfigHandler().getConfig().weapons.rollDelaySeconds;
                final String rollStartTitle = plugin.getConfigHandler().getConfig().weapons.rollStartTitle;
                
                // Run the roll title effect after the configured delay
                new WeaponRollTask(player, available, strengthService, rollStartTitle)
                    .runTaskTimer(plugin, delaySeconds * 20L, 4L);
            }
        }
    }
}
