package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.service.StrengthService;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener handling Mace Ultimate active state, auto-enchantments, zero-cooldown, and PDC cleanup guards.
 */
public final class MaceAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final NamespacedKey TEMP_ULT_KEY = new NamespacedKey("strength", "temp_ult_mace");
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> activeUltimatePlayers = new ConcurrentHashMap<>();

    public MaceAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    /**
     * Applies temporary ultimate auto-enchantments to any Mace held by the active ultimate player.
     */
    public static void applyTemporaryEnchantments(@NotNull Player player, @NotNull MaceConfig.UltimateConfig settings) {
        final ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() != Material.MACE) return;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(TEMP_ULT_KEY, PersistentDataType.BYTE)) return; // Already enchanted

        pdc.set(TEMP_ULT_KEY, PersistentDataType.BYTE, (byte) 1);

        for (String entry : settings.autoEnchantments) {
            if (entry == null || entry.isBlank()) continue;
            final String[] parts = entry.split(":");
            if (parts.length < 2) continue;

            final String enchKey = parts[0].trim().toLowerCase();
            final int level;
            try {
                level = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                continue;
            }

            final NamespacedKey key = NamespacedKey.minecraft(enchKey);
            final Enchantment enchantment = Registry.ENCHANTMENT.get(key);
            if (enchantment != null) {
                meta.addEnchant(enchantment, level, true);
            }
        }

        item.setItemMeta(meta);
    }

    /**
     * Strips temporary ultimate auto-enchantments from a specific ItemStack.
     */
    public static void stripTemporaryEnchantments(@Nullable ItemStack item, @NotNull MaceConfig.UltimateConfig settings) {
        if (item == null || item.getType() != Material.MACE) return;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(TEMP_ULT_KEY, PersistentDataType.BYTE)) return; // Not a temp ult item

        pdc.remove(TEMP_ULT_KEY);

        for (String entry : settings.autoEnchantments) {
            if (entry == null || entry.isBlank()) continue;
            final String[] parts = entry.split(":");
            final String enchKey = parts[0].trim().toLowerCase();

            final NamespacedKey key = NamespacedKey.minecraft(enchKey);
            final Enchantment enchantment = Registry.ENCHANTMENT.get(key);
            if (enchantment != null) {
                meta.removeEnchant(enchantment);
            }
        }

        item.setItemMeta(meta);
    }

    /**
     * Strips temporary ultimate enchantments from all items in a player's inventory.
     */
    public static void stripTemporaryEnchantmentsFromPlayer(@NotNull Player player, @NotNull MaceConfig.UltimateConfig settings) {
        for (ItemStack item : player.getInventory().getContents()) {
            stripTemporaryEnchantments(item, settings);
        }
    }

    /* =========================================================================
     * Event Guards for Instant Exploit Prevention
     * ========================================================================= */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        stripTemporaryEnchantments(droppedItem.getItemStack(), settings.ultimate);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        stripTemporaryEnchantments(event.getCurrentItem(), settings.ultimate);
        stripTemporaryEnchantments(event.getCursor(), settings.ultimate);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        for (ItemStack item : event.getNewItems().values()) {
            stripTemporaryEnchantments(item, settings.ultimate);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final UUID uuid = player.getUniqueId();
        activeUltimatePlayers.remove(uuid);

        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        stripTemporaryEnchantmentsFromPlayer(player, settings.ultimate);
        for (ItemStack drop : event.getDrops()) {
            stripTemporaryEnchantments(drop, settings.ultimate);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        activeUltimatePlayers.remove(uuid);
        ultimateCooldowns.remove(uuid);

        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        stripTemporaryEnchantmentsFromPlayer(player, settings.ultimate);
    }
}
