package lunatech.strength.gui;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.RerollRecipeSettings;
import lunatech.strength.utility.ItemResolver;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Manager responsible for opening the immutable Virtual Workbench Crafting GUI showing the Reroll Book recipe.
 */
public final class RerollRecipeGui {

    private RerollRecipeGui() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void open(@NotNull Strength plugin, @NotNull Player player) {
        final RerollRecipeGuiHolder holder = new RerollRecipeGuiHolder();
        final lunatech.strength.config.WeaponsGuiConfig.GuiSlotItemConfig rerollCfg = plugin.getConfigHandler().getWeaponsGuiConfig().rerollItem;
        final String rawTitle = (rerollCfg != null && rerollCfg.recipeGuiTitle != null && !rerollCfg.recipeGuiTitle.trim().isEmpty())
            ? rerollCfg.recipeGuiTitle
            : "<light_purple><bold>Recipe: Weapon Reroll Book</bold></light_purple>";

        final Inventory inv = Bukkit.createInventory(
            holder,
            InventoryType.WORKBENCH,
            MiniMessage.miniMessage().deserialize(rawTitle).decoration(TextDecoration.ITALIC, false)
        );
        holder.setInventory(inv);

        // 1. Result slot (slot 0) = Reroll Book output item
        final ItemStack rerollResult = plugin.getStrengthService().createRerollItem();
        inv.setItem(0, rerollResult);

        // 2. Crafting Grid slots (slots 1 to 9 in 3x3 matrix)
        if (recipe != null) {
            final List<String> shape = recipe.shape;
            final Map<String, String> ingredients = recipe.ingredients;

            if (shape != null && ingredients != null) {
                for (int row = 0; row < 3 && row < shape.size(); row++) {
                    final String line = shape.get(row);
                    for (int col = 0; col < 3 && col < line.length(); col++) {
                        final char keyChar = line.charAt(col);
                        final String key = String.valueOf(keyChar);
                        final String ingredientStr = ingredients.get(key);

                        if (ingredientStr != null) {
                            final ItemStack item = ItemResolver.resolveItemStack(ingredientStr, plugin.getStrengthService());
                            final int slot = (row * 3) + col + 1; // slots 1 to 9
                            if (item != null) {
                                inv.setItem(slot, item);
                            }
                        }
                    }
                }
            }
        }

        player.openInventory(inv);
    }
}
