package lunatech.strength.recipe;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.RecipeSettings;
import lunatech.strength.config.PluginConfig.RerollRecipeSettings;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.utility.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;

import java.util.List;
import java.util.Map;

/**
 * Handler responsible for registering and unregistering craftable item recipes (Strength Item & Weapon Reroll Book).
 */
public class RecipeHandler implements Reloadable {
    private final Strength plugin;

    public RecipeHandler(Strength plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable(AbstractStrength abstractPlugin) {
        // Remove existing recipes if present to handle reloads cleanly
        Bukkit.removeRecipe(PDCKeys.STRENGTH_RECIPE);
        Bukkit.removeRecipe(PDCKeys.REROLL_RECIPE);

        registerStrengthRecipe();
        registerRerollRecipe();
    }

    private void registerStrengthRecipe() {
        final RecipeSettings recipeSettings = plugin.getConfigHandler().getConfig().recipe;
        if (!recipeSettings.enabled) {
            return;
        }

        try {
            final ItemStack result = plugin.getStrengthService().createStrengthItem(recipeSettings.resultStrengthAmount);
            final ShapedRecipe recipe = new ShapedRecipe(PDCKeys.STRENGTH_RECIPE, result);

            final List<String> shapeList = recipeSettings.shape;
            if (shapeList != null && shapeList.size() == 3) {
                recipe.shape(shapeList.get(0), shapeList.get(1), shapeList.get(2));
            } else {
                recipe.shape("DGD", "GNG", "DGD");
            }

            final Map<String, String> ingredients = recipeSettings.ingredients;
            if (ingredients != null) {
                for (Map.Entry<String, String> entry : ingredients.entrySet()) {
                    final String keyStr = entry.getKey();
                    if (keyStr == null || keyStr.isEmpty()) continue;
                    final char keyChar = keyStr.charAt(0);
                    final String val = entry.getValue();

                    final RecipeChoice choice = lunatech.strength.utility.ItemResolver.resolveRecipeChoice(val, plugin.getStrengthService());
                    if (choice != null) {
                        recipe.setIngredient(keyChar, choice);
                    }
                }
            }

            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            Logger.get().warn("Failed to register Strength Item crafting recipe: {}", e.getMessage());
        }
    }

    private void registerRerollRecipe() {
        final RerollRecipeSettings rerollSettings = plugin.getConfigHandler().getConfig().rerollRecipe;
        if (!rerollSettings.enabled) {
            return;
        }

        try {
            final ItemStack result = plugin.getStrengthService().createRerollItem();
            final ShapedRecipe recipe = new ShapedRecipe(PDCKeys.REROLL_RECIPE, result);

            final List<String> shapeList = rerollSettings.shape;
            if (shapeList != null && shapeList.size() == 3) {
                recipe.shape(shapeList.get(0), shapeList.get(1), shapeList.get(2));
            } else {
                recipe.shape("SSS", "SBS", "SSS");
            }

            final Map<String, String> ingredients = rerollSettings.ingredients;
            if (ingredients != null) {
                for (Map.Entry<String, String> entry : ingredients.entrySet()) {
                    final String keyStr = entry.getKey();
                    if (keyStr == null || keyStr.isEmpty()) continue;
                    final char keyChar = keyStr.charAt(0);
                    final String val = entry.getValue();

                    final RecipeChoice choice = lunatech.strength.utility.ItemResolver.resolveRecipeChoice(val, plugin.getStrengthService());
                    if (choice != null) {
                        recipe.setIngredient(keyChar, choice);
                    }
                }
            }

            Bukkit.addRecipe(recipe);
        } catch (Exception e) {
            Logger.get().warn("Failed to register Weapon Reroll Book crafting recipe: {}", e.getMessage());
        }
    }

    @Override
    public void onDisable(AbstractStrength abstractPlugin) {
        Bukkit.removeRecipe(PDCKeys.STRENGTH_RECIPE);
        Bukkit.removeRecipe(PDCKeys.REROLL_RECIPE);
    }
}
