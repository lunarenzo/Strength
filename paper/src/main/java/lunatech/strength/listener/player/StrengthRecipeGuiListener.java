package lunatech.strength.listener.player;

import lunatech.strength.gui.StrengthRecipeGuiHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that cancels all clicks and drags in the virtual Workbench Strength Recipe GUI to keep it 100% immutable.
 */
public final class StrengthRecipeGuiListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof StrengthRecipeGuiHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof StrengthRecipeGuiHolder) {
            event.setCancelled(true);
        }
    }
}
