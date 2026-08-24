package lunatech.strength.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Custom InventoryHolder marker for the Strength Weapons GUI.
 * Eliminates fragile string title comparisons and prevents inventory interaction exploits.
 */
public final class WeaponsGuiHolder implements InventoryHolder {
    private Inventory inventory;

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
