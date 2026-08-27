package lunatech.strength.utility;

import lunatech.strength.config.EnchantmentConfig;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ultra-low-latency utility for sanitizing and capping item enchantments
 * according to server owner rules in `enchantment-restrictions.yml`.
 */
public final class EnchantmentSanitizer {

    private EnchantmentSanitizer() {
    }

    /**
     * Sanitizes an ItemStack in-place if its enchantments violate server restriction rules.
     * Handles both regular items and Enchanted Books (EnchantmentStorageMeta).
     *
     * @param item   the item stack to sanitize
     * @param config the enchantment configuration
     * @return true if the item was modified/sanitized, false otherwise
     */
    public static boolean sanitizeItem(@Nullable ItemStack item, @NotNull EnchantmentConfig config) {
        if (item == null || item.getType().isAir() || !config.enabled) {
            return false;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            final Map<Enchantment, Integer> current = storageMeta.getStoredEnchants();
            if (current.isEmpty()) {
                return false;
            }
            final Map<Enchantment, Integer> sanitized = sanitizeEnchantmentMap(current, config);
            if (current.equals(sanitized)) {
                return false;
            }
            // Clear existing stored enchantments and apply sanitized set
            for (Enchantment ench : List.copyOf(current.keySet())) {
                storageMeta.removeStoredEnchant(ench);
            }
            for (Map.Entry<Enchantment, Integer> entry : sanitized.entrySet()) {
                storageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
            }
            item.setItemMeta(storageMeta);
            return true;
        } else {
            final Map<Enchantment, Integer> current = meta.getEnchants();
            if (current.isEmpty()) {
                return false;
            }
            final Map<Enchantment, Integer> sanitized = sanitizeEnchantmentMap(current, config);
            if (current.equals(sanitized)) {
                return false;
            }
            // Clear existing enchantments and apply sanitized set
            for (Enchantment ench : List.copyOf(current.keySet())) {
                meta.removeEnchant(ench);
            }
            for (Map.Entry<Enchantment, Integer> entry : sanitized.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            item.setItemMeta(meta);
            return true;
        }
    }

    /**
     * Sanitizes a map of enchantments and their levels according to config rules.
     *
     * @param source the source map of enchantments to levels
     * @param config the enchantment configuration
     * @return a clean, sanitized map of enchantments
     */
    public static Map<Enchantment, Integer> sanitizeEnchantmentMap(
        @NotNull Map<Enchantment, Integer> source,
        @NotNull EnchantmentConfig config
    ) {
        if (source.isEmpty() || !config.enabled) {
            return source;
        }

        final Map<Enchantment, Integer> result = new HashMap<>();
        final boolean isWhitelist = "WHITELIST".equalsIgnoreCase(config.mode);

        for (Map.Entry<Enchantment, Integer> entry : source.entrySet()) {
            final Enchantment ench = entry.getKey();
            if (ench == null) {
                continue;
            }
            int level = entry.getValue();

            final String key = ench.getKey().getKey().toLowerCase();

            if (isWhitelist) {
                if (config.whitelistedEnchantments != null && !containsIgnoreCase(config.whitelistedEnchantments, key)) {
                    continue;
                }
            } else {
                if (config.blacklistedEnchantments != null && containsIgnoreCase(config.blacklistedEnchantments, key)) {
                    continue;
                }
            }

            if (config.maxLevels != null) {
                for (Map.Entry<String, Integer> maxEntry : config.maxLevels.entrySet()) {
                    if (maxEntry.getKey().equalsIgnoreCase(key)) {
                        final int max = maxEntry.getValue();
                        if (level > max) {
                            level = max;
                        }
                        break;
                    }
                }
            }

            if (level > 0) {
                result.put(ench, level);
            }
        }

        return result;
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }
}
