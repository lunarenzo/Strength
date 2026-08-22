package lunatech.strength.utility;

import lunatech.strength.service.StrengthService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * High-performance resolver for item configurations supporting Bukkit Materials, CustomModelData,
 * ItemsAdder, Nexo, Oraxen, and plugin custom Strength Shards.
 */
public final class ItemResolver {

    private ItemResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Resolves a string configuration value to a Bukkit ItemStack.
     *
     * @param input raw string from config
     * @param strengthService plugin StrengthService for STRENGTH_ITEM fallback
     * @return resolved ItemStack or null if unresolvable
     */
    @Nullable
    public static ItemStack resolveItemStack(@Nullable String input, @Nullable StrengthService strengthService) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }

        final String trimmed = input.trim();

        // 1. Internal plugin custom items
        if ("STRENGTH_ITEM".equalsIgnoreCase(trimmed) || "STRENGTH_SHARD".equalsIgnoreCase(trimmed)) {
            return strengthService != null ? strengthService.createStrengthItem(1) : null;
        }

        // 2. ItemsAdder support (Format: itemsadder:namespace:item_id or itemsadder:item_id)
        if (trimmed.toLowerCase().startsWith("itemsadder:")) {
            final String iaId = trimmed.substring("itemsadder:".length());
            return resolveItemsAdder(iaId);
        }

        // 3. Nexo support (Format: nexo:item_id)
        if (trimmed.toLowerCase().startsWith("nexo:")) {
            final String nexoId = trimmed.substring("nexo:".length());
            return resolveNexo(nexoId);
        }

        // 4. Oraxen support (Format: oraxen:item_id)
        if (trimmed.toLowerCase().startsWith("oraxen:")) {
            final String oraxenId = trimmed.substring("oraxen:".length());
            return resolveOraxen(oraxenId);
        }

        // 5. CustomModelData support (Format: cmd:12345:MATERIAL or custom_model_data:12345:MATERIAL)
        if (trimmed.toLowerCase().startsWith("cmd:") || trimmed.toLowerCase().startsWith("custom_model_data:")) {
            final String[] parts = trimmed.split(":");
            if (parts.length >= 3) {
                try {
                    final int cmd = Integer.parseInt(parts[1]);
                    final Material mat = Material.matchMaterial(parts[2]);
                    if (mat != null) {
                        final ItemStack stack = new ItemStack(mat);
                        final ItemMeta meta = stack.getItemMeta();
                        if (meta != null) {
                            meta.setCustomModelData(cmd);
                            stack.setItemMeta(meta);
                        }
                        return stack;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // 6. Standard Bukkit Material
        final Material mat = Material.matchMaterial(trimmed);
        if (mat != null) {
            return new ItemStack(mat);
        }

        return null;
    }

    /**
     * Resolves a string configuration value to a RecipeChoice for crafting recipes.
     *
     * @param input raw string from config
     * @param strengthService plugin StrengthService for STRENGTH_ITEM fallback
     * @return resolved RecipeChoice or null
     */
    @Nullable
    public static RecipeChoice resolveRecipeChoice(@Nullable String input, @Nullable StrengthService strengthService) {
        final ItemStack stack = resolveItemStack(input, strengthService);
        if (stack == null) {
            return null;
        }

        // If stack has custom item meta (e.g. custom model data or PDC), exact choice is required
        if (stack.hasItemMeta()) {
            return new RecipeChoice.ExactChoice(stack);
        }
        return new RecipeChoice.MaterialChoice(stack.getType());
    }

    private static ItemStack resolveItemsAdder(String id) {
        if (!Bukkit.getPluginManager().isPluginEnabled("ItemsAdder")) {
            return null;
        }
        try {
            final Class<?> clazz = Class.forName("dev.lone.itemsadder.api.CustomStack");
            final Method getInstance = clazz.getMethod("getInstance", String.class);
            final Object customStack = getInstance.invoke(null, id);
            if (customStack != null) {
                final Method getItemStack = clazz.getMethod("getItemStack");
                return (ItemStack) getItemStack.invoke(customStack);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static ItemStack resolveNexo(String id) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            return null;
        }
        try {
            final Class<?> clazz = Class.forName("com.nexomc.nexo.api.NexoItems");
            final Method itemFromId = clazz.getMethod("itemFromId", String.class);
            final Object builder = itemFromId.invoke(null, id);
            if (builder != null) {
                final Method build = builder.getClass().getMethod("build");
                return (ItemStack) build.invoke(builder);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static ItemStack resolveOraxen(String id) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Oraxen")) {
            return null;
        }
        try {
            final Class<?> clazz = Class.forName("io.th0rgal.oraxen.api.OraxenItems");
            final Method getItemById = clazz.getMethod("getItemById", String.class);
            final Object builder = getItemById.invoke(null, id);
            if (builder != null) {
                final Method build = builder.getClass().getMethod("build");
                return (ItemStack) build.invoke(builder);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
