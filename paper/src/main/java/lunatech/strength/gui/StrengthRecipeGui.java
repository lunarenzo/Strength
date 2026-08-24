package lunatech.strength.gui;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.RecipeSettings;
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
 * Manager responsible for opening the immutable Virtual Workbench Crafting GUI showing the Strength Item recipe.
 */
public final class StrengthRecipeGui {

    private StrengthRecipeGui() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void open(@NotNull Strength plugin, @NotNull Player player) {
        final StrengthRecipeGuiHolder holder = new StrengthRecipeGuiHolder();
        final lunatech.strength.config.WeaponsGuiConfig.GuiSlotItemConfig strengthCfg = plugin.getConfigHandler().getWeaponsGuiConfig().strengthItem;
        final String rawTitle = (strengthCfg != null && strengthCfg.recipeGuiTitle != null && !strengthCfg.recipeGuiTitle.trim().isEmpty())
            ? strengthCfg.recipeGuiTitle
            : "<dark_purple><bold>Recipe: Strength Shard</bold></dark_purple>";

        final Inventory inv = Bukkit.createInventory(
            holder,
            InventoryType.WORKBENCH,
            MiniMessage.miniMessage().deserialize(rawTitle).decoration(TextDecoration.ITALIC, false)
        );
        holder.setInventory(inv);

        final int amount = recipe != null ? recipe.resultStrengthAmount : 1;

        // 1. Result slot (slot 0) = Strength Item output item
        final ItemStack strengthResult = plugin.getStrengthService().createStrengthItem(amount);
        inv.setItem(0, strengthResult);

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
