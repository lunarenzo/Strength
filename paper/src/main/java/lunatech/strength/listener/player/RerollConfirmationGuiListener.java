package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.ChestGuiSettings;
import lunatech.strength.gui.RerollConfirmationGui;
import lunatech.strength.gui.RerollConfirmationHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Listener that intercepts clicks in native RerollConfirmationHolder chest inventories.
 */
public final class RerollConfirmationGuiListener implements Listener {
    private final Strength plugin;

    public RerollConfirmationGuiListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof RerollConfirmationHolder)) {
            return;
        }

        // Cancel all item movements in confirmation GUI
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        final int clickedSlot = event.getRawSlot();
        final ChestGuiSettings guiSettings = plugin.getConfigHandler().getConfig().rerollDialog.gui;

        if (clickedSlot == guiSettings.confirmSlot) {
            player.closeInventory();
            RerollConfirmationGui.executeReroll(plugin, player);
        } else if (clickedSlot == guiSettings.cancelSlot) {
            player.closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof RerollConfirmationHolder) {
            event.setCancelled(true);
        }
    }
}
