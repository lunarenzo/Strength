package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.WeaponsGuiConfig;
import lunatech.strength.gui.RerollRecipeGui;
import lunatech.strength.gui.WeaponsGuiHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that intercepts clicks in native WeaponsGuiHolder chest inventories.
 */
public final class WeaponsGuiListener implements Listener {
    private final Strength plugin;

    public WeaponsGuiListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WeaponsGuiHolder) {
            event.setCancelled(true);

            final WeaponsGuiConfig config = plugin.getConfigHandler().getWeaponsGuiConfig();
            if (config != null && config.rerollItem != null) {
                if (event.getSlot() == config.rerollItem.slot && event.getWhoClicked() instanceof Player player) {
                    player.closeInventory();
                    RerollRecipeGui.open(plugin, player);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof WeaponsGuiHolder) {
            event.setCancelled(true);
        }
    }
}
