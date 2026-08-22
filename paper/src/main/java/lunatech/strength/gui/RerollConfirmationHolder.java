package lunatech.strength.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Custom InventoryHolder tag for identifying the Reroll Confirmation Inventory cleanly.
 */
public final class RerollConfirmationHolder implements InventoryHolder {
    private Inventory inventory;

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
