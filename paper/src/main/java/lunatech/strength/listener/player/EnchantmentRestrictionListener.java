package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.EnchantmentConfig;
import lunatech.strength.utility.EnchantmentSanitizer;
import org.bukkit.entity.Player;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Ultra-low-latency event listener enforcing global enchantment restrictions
 * across Enchanting Tables, Anvils, Smithing Tables, Villager Trades, Loot Tables, Mobs, and Pickups.
 */
public final class EnchantmentRestrictionListener implements Listener {
    private final Strength plugin;

    public EnchantmentRestrictionListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    private EnchantmentConfig getConfig() {
        return plugin.getConfigHandler().getEnchantmentConfig();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(@NotNull EnchantItemEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final Map<Enchantment, Integer> enchantsToAdd = event.getEnchantsToAdd();
        if (enchantsToAdd == null || enchantsToAdd.isEmpty()) {
            return;
        }

        final Map<Enchantment, Integer> sanitized = EnchantmentSanitizer.sanitizeEnchantmentMap(enchantsToAdd, config);
        if (!enchantsToAdd.equals(sanitized)) {
            enchantsToAdd.clear();
            enchantsToAdd.putAll(sanitized);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(@NotNull PrepareAnvilEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final ItemStack result = event.getResult();
        if (result != null && !result.getType().isAir()) {
            if (EnchantmentSanitizer.sanitizeItem(result, config)) {
                event.setResult(result);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareSmithing(@NotNull PrepareSmithingEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final ItemStack result = event.getResult();
        if (result != null && !result.getType().isAir()) {
            if (EnchantmentSanitizer.sanitizeItem(result, config)) {
                event.setResult(result);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVillagerAcquireTrade(@NotNull VillagerAcquireTradeEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled || !config.blockInTrades) {
            return;
        }

        final ItemStack result = event.getRecipe().getResult();
        if (result != null && !result.getType().isAir()) {
            EnchantmentSanitizer.sanitizeItem(result, config);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLootGenerate(@NotNull LootGenerateEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled || !config.blockInLoot) {
            return;
        }

        final List<ItemStack> loot = event.getLoot();
        if (loot != null && !loot.isEmpty()) {
            for (int i = 0; i < loot.size(); i++) {
                final ItemStack item = loot.get(i);
                if (item != null && !item.getType().isAir()) {
                    EnchantmentSanitizer.sanitizeItem(item, config);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled || !config.blockInLoot) {
            return;
        }

        final List<ItemStack> drops = event.getDrops();
        if (drops != null && !drops.isEmpty()) {
            for (int i = 0; i < drops.size(); i++) {
                final ItemStack item = drops.get(i);
                if (item != null && !item.getType().isAir()) {
                    EnchantmentSanitizer.sanitizeItem(item, config);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        final EnchantmentConfig config = getConfig();
        if (!config.enabled || !config.blockOnPickup) {
            return;
        }

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        final ItemStack item = event.getItem().getItemStack();
        if (item != null && !item.getType().isAir()) {
            if (EnchantmentSanitizer.sanitizeItem(item, config)) {
                event.getItem().setItemStack(item);
            }
        }
    }
}
