package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.config.PluginConfig.MessagesConfig;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Listener handling Mace limits, smash attack cooldowns, enchanting rules, and container restrictions.
 */
public final class MaceListener implements Listener {
    private final Strength plugin;

    public MaceListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    private MaceConfig getConfig() {
        return plugin.getConfigHandler().getMaceConfig();
    }

    private MessagesConfig getMessages() {
        return plugin.getConfigHandler().getConfig().messages;
    }

    /* =========================================================================
     * 1. Mace Limit & Crafting Logic
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareItemCraft(@NotNull PrepareItemCraftEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final ItemStack result = event.getInventory().getResult();

        // Guard: Prevent packing Mace into Shulker Boxes via craft matrix when container storage is forbidden
        if (config.container.enabled && !config.container.allowStorage) {
            if (result != null && result.getType().name().contains("SHULKER_BOX")) {
                for (ItemStack ingredient : event.getInventory().getMatrix()) {
                    if (ingredient != null && ingredient.getType() == Material.MACE) {
                        event.getInventory().setResult(null);
                        return;
                    }
                }
            }
        }

        // Mace crafting limit checks
        if (!config.limit.enabled || result == null || result.getType() != Material.MACE) {
            return;
        }

        if (config.limit.maxAmount <= 0) {
            event.getInventory().setResult(null);
            return;
        }

        final Server server = event.getView().getPlayer().getServer();
        if (countGlobalMaces(server) >= config.limit.maxAmount) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCraftItem(@NotNull CraftItemEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final ItemStack currentItem = event.getCurrentItem();

        // Block Shulker Box crafting with Mace ingredient when container storage is forbidden
        if (config.container.enabled && !config.container.allowStorage && currentItem != null && currentItem.getType().name().contains("SHULKER_BOX")) {
            for (ItemStack ingredient : event.getInventory().getMatrix()) {
                if (ingredient != null && ingredient.getType() == Material.MACE) {
                    event.setCancelled(true);
                    MessageUtil.send(player, getMessages().maceContainerForbiddenMessage);
                    return;
                }
            }
        }

        if (!config.limit.enabled || currentItem == null || currentItem.getType() != Material.MACE) {
            return;
        }

        if (config.limit.maxAmount <= 0) {
            event.setCancelled(true);
            removeMacesFromPlayer(player);
            MessageUtil.send(player, getMessages().maceDisabledMessage);
            return;
        }

        final Server server = player.getServer();
        if (countGlobalMaces(server) >= config.limit.maxAmount) {
            event.setCancelled(true);
            MessageUtil.send(player, getMessages().maceLimitCraftBlockedMessage, "limit", String.valueOf(config.limit.maxAmount));
        }
    }

    /* =========================================================================
     * 2. Mace Attack & Cooldown Logic
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        final ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getType() != Material.MACE) {
            return;
        }

        // Check Mace limit == 0 rule
        if (config.limit.enabled && config.limit.maxAmount <= 0) {
            event.setCancelled(true);
            removeMacesFromPlayer(player);
            MessageUtil.send(player, getMessages().maceDisabledMessage);
            return;
        }

        // Enforce limit if an illegal excess mace exists on player
        if (config.limit.enabled && config.limit.maxAmount > 0) {
            enforceExcessMaceLimit(player, config.limit.maxAmount);
        }

        // Check Mace Cooldown for Smash Attack
        if (config.cooldown.enabled) {
            final boolean isSmashAttack = player.getFallDistance() > 1.5;
            if (isSmashAttack) {
                if (player.hasCooldown(Material.MACE)) {
                    event.setCancelled(true);
                    final long remainingSec = (player.getCooldown(Material.MACE) + 19) / 20;
                    MessageUtil.send(player, getMessages().maceCooldownMessage, "seconds", String.valueOf(remainingSec));
                } else {
                    player.setCooldown(Material.MACE, config.cooldown.cooldownSeconds * 20);
                }
            }
        }
    }

    /* =========================================================================
     * 3. Mace Interaction, Slot Change & Pickup Guards
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.limit.enabled) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.MACE) {
            return;
        }

        if (config.limit.maxAmount <= 0) {
            event.setCancelled(true);
            removeMacesFromPlayer(player);
            MessageUtil.send(player, getMessages().maceDisabledMessage);
        } else {
            enforceExcessMaceLimit(player, config.limit.maxAmount);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemHeld(@NotNull PlayerItemHeldEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.limit.enabled) {
            return;
        }

        final Player player = event.getPlayer();
        final ItemStack stack = player.getInventory().getItem(event.getNewSlot());
        if (stack == null || stack.getType() != Material.MACE) {
            return;
        }

        if (config.limit.maxAmount <= 0) {
            removeMacesFromPlayer(player);
            MessageUtil.send(player, getMessages().maceDisabledMessage);
        } else {
            enforceExcessMaceLimit(player, config.limit.maxAmount);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.limit.enabled) {
            return;
        }

        if (event.getItem().getItemStack().getType() != Material.MACE) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (config.limit.maxAmount <= 0) {
            event.setCancelled(true);
            event.getItem().remove();
            MessageUtil.send(player, getMessages().maceDisabledMessage);
            return;
        }

        final Server server = player.getServer();
        if (countGlobalMaces(server) >= config.limit.maxAmount) {
            event.setCancelled(true);
            MessageUtil.send(player, getMessages().maceLimitCraftBlockedMessage, "limit", String.valueOf(config.limit.maxAmount));
        }
    }

    /* =========================================================================
     * 4. Mace Enchanting Restrictions
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareItemEnchant(@NotNull PrepareItemEnchantEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.enchant.enabled) {
            return;
        }

        if (event.getItem().getType() == Material.MACE && !config.enchant.allowEnchanting) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(@NotNull EnchantItemEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.enchant.enabled) {
            return;
        }

        if (event.getItem().getType() != Material.MACE) {
            return;
        }

        final Player player = event.getEnchanter();
        if (!config.enchant.allowEnchanting) {
            event.setCancelled(true);
            MessageUtil.send(player, getMessages().maceEnchantForbiddenMessage);
            return;
        }

        if (isEnchantmentRestricted(event.getEnchantsToAdd(), config.enchant)) {
            event.setCancelled(true);
            MessageUtil.send(player, getMessages().maceEnchantForbiddenMessage);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareAnvil(@NotNull PrepareAnvilEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.enchant.enabled) {
            return;
        }

        final AnvilInventory inv = event.getInventory();
        final ItemStack first = inv.getItem(0);
        final ItemStack second = inv.getItem(1);
        final ItemStack result = event.getResult();

        final boolean involvesMace = (first != null && first.getType() == Material.MACE)
                || (second != null && second.getType() == Material.MACE)
                || (result != null && result.getType() == Material.MACE);

        if (!involvesMace) {
            return;
        }

        final boolean allowEnchanting = config.enchant.allowEnchanting;
        final boolean allowRenaming = config.enchant.allowRenaming;

        if (!allowEnchanting) {
            final boolean secondHasEnchants = second != null && second.getType() != Material.AIR
                    && (!second.getEnchantments().isEmpty() || second.getType() == Material.ENCHANTED_BOOK);

            final boolean isAddingOrUpgradingEnchants = hasNewOrUpgradedEnchantments(first, result) || secondHasEnchants;

            if (isAddingOrUpgradingEnchants) {
                event.setResult(null);
                return;
            }

            if (!allowRenaming) {
                event.setResult(null);
            }
            return;
        }

        if (result != null && isEnchantmentRestricted(result.getEnchantments(), config.enchant)) {
            event.setResult(null);
        }
    }

    /* =========================================================================
     * 5. Mace Container Storage Restrictions & Slot Guards
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final ItemStack currentItem = event.getCurrentItem();
        final ItemStack cursorItem = event.getCursor();

        // Limit 0 cleanup check
        if (config.limit.enabled && config.limit.maxAmount <= 0) {
            if ((currentItem != null && currentItem.getType() == Material.MACE)
                    || (cursorItem != null && cursorItem.getType() == Material.MACE)) {
                event.setCancelled(true);
                removeMacesFromPlayer(player);
                MessageUtil.send(player, getMessages().maceDisabledMessage);
                return;
            }
        }

        final Inventory topInv = event.getView().getTopInventory();
        final InventoryType topType = topInv.getType();

        // Enchanting Table & Anvil slot placement guards
        if (config.enchant.enabled) {
            if (topType == InventoryType.ENCHANTING && !config.enchant.allowEnchanting) {
                final boolean isTopSlot = event.getClickedInventory() == topInv;
                final boolean isShiftClickingMace = event.isShiftClick() && currentItem != null && currentItem.getType() == Material.MACE;
                final boolean isPlacingMaceInTop = isTopSlot && ((cursorItem != null && cursorItem.getType() == Material.MACE) || (currentItem != null && currentItem.getType() == Material.MACE));

                if (isShiftClickingMace || isPlacingMaceInTop) {
                    event.setCancelled(true);
                    MessageUtil.send(player, getMessages().maceEnchantForbiddenMessage);
                    return;
                }
            } else if (topType == InventoryType.ANVIL && !config.enchant.allowEnchanting && !config.enchant.allowRenaming) {
                final boolean isTopSlot = event.getClickedInventory() == topInv;
                final boolean isShiftClickingMace = event.isShiftClick() && currentItem != null && currentItem.getType() == Material.MACE;
                final boolean isPlacingMaceInTop = isTopSlot && ((cursorItem != null && cursorItem.getType() == Material.MACE) || (currentItem != null && currentItem.getType() == Material.MACE));

                if (isShiftClickingMace || isPlacingMaceInTop) {
                    event.setCancelled(true);
                    MessageUtil.send(player, getMessages().maceEnchantForbiddenMessage);
                    return;
                }
            }
        }

        // Container storage restriction check
        if (config.container.enabled) {
            if (isContainerRestricted(topInv, config.container)) {
                final boolean isTopSlot = event.getClickedInventory() == topInv;
                final boolean isShiftClickingMace = event.isShiftClick() && currentItem != null && currentItem.getType() == Material.MACE;
                final boolean isPlacingMaceInTop = isTopSlot && ((cursorItem != null && cursorItem.getType() == Material.MACE) || (currentItem != null && currentItem.getType() == Material.MACE));

                if (isShiftClickingMace || isPlacingMaceInTop) {
                    event.setCancelled(true);
                    MessageUtil.send(player, getMessages().maceContainerForbiddenMessage);
                    return;
                }
            }

            // Bundle storage guard
            final boolean isBundleAndMace = (currentItem != null && isBundle(currentItem.getType()) && cursorItem != null && cursorItem.getType() == Material.MACE)
                    || (cursorItem != null && isBundle(cursorItem.getType()) && currentItem != null && currentItem.getType() == Material.MACE);
            if (isBundleAndMace && !config.container.allowStorage) {
                event.setCancelled(true);
                MessageUtil.send(player, getMessages().maceContainerForbiddenMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        if (event.getOldCursor().getType() != Material.MACE) {
            return;
        }

        final Inventory topInv = event.getView().getTopInventory();
        final InventoryType topType = topInv.getType();

        // Anvil & Enchanting Table drag guard
        if (config.enchant.enabled && !config.enchant.allowEnchanting) {
            if (topType == InventoryType.ANVIL || topType == InventoryType.ENCHANTING) {
                final int topSize = topInv.getSize();
                for (int slot : event.getRawSlots()) {
                    if (slot < topSize) {
                        event.setCancelled(true);
                        MessageUtil.send((Player) event.getWhoClicked(), getMessages().maceEnchantForbiddenMessage);
                        return;
                    }
                }
            }
        }

        // Container storage drag guard
        if (config.container.enabled && isContainerRestricted(topInv, config.container)) {
            final int topSize = topInv.getSize();
            for (int slot : event.getRawSlots()) {
                if (slot < topSize) {
                    event.setCancelled(true);
                    MessageUtil.send((Player) event.getWhoClicked(), getMessages().maceContainerForbiddenMessage);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMoveItem(@NotNull InventoryMoveItemEvent event) {
        final MaceConfig config = getConfig();
        if (!config.enabled || !config.container.enabled) {
            return;
        }

        if (event.getItem().getType() != Material.MACE) {
            return;
        }

        if (isContainerRestricted(event.getDestination(), config.container)) {
            event.setCancelled(true);
        }
    }

    /* =========================================================================
     * Utility Helpers
     * ========================================================================= */

    private boolean isBundle(@NotNull Material material) {
        return material.name().contains("BUNDLE");
    }

    private boolean isContainerRestricted(@NotNull Inventory inventory, @NotNull MaceConfig.ContainerConfig config) {
        if (inventory.getType() == InventoryType.PLAYER || inventory.getType() == InventoryType.CRAFTING || inventory.getType() == InventoryType.CREATIVE) {
            return false;
        }

        if (!config.allowStorage) {
            final String invTypeName = inventory.getType().name().toUpperCase();
            String blockOrEntityName = "";
            if (inventory.getHolder() instanceof BlockState blockState) {
                blockOrEntityName = blockState.getType().name().toUpperCase();
            } else if (inventory.getHolder() instanceof org.bukkit.entity.Entity entity) {
                blockOrEntityName = entity.getType().name().toUpperCase();
            }

            final boolean matchesList = matchesContainerList(invTypeName, config.containers) || matchesContainerList(blockOrEntityName, config.containers);
            final boolean isBlacklist = "BLACKLIST".equalsIgnoreCase(config.mode);

            if (isBlacklist) {
                return matchesList || config.containers.isEmpty();
            } else {
                return !matchesList;
            }
        }
        return false;
    }

    private boolean matchesContainerList(@NotNull String name, @NotNull java.util.List<String> list) {
        if (name.isEmpty()) {
            return false;
        }
        for (String entry : list) {
            if (name.contains(entry.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isEnchantmentRestricted(@NotNull Map<Enchantment, Integer> enchants, @NotNull MaceConfig.EnchantConfig config) {
        if (enchants.isEmpty()) {
            return false;
        }

        final boolean isBlacklist = "BLACKLIST".equalsIgnoreCase(config.mode);

        for (Enchantment enchant : enchants.keySet()) {
            final String key = enchant.getKey().toString().toLowerCase();
            final String name = enchant.getKey().getKey().toLowerCase();

            boolean matched = false;
            for (String entry : config.enchantments) {
                final String lowerEntry = entry.toLowerCase();
                if (key.equals(lowerEntry) || name.equals(lowerEntry)) {
                    matched = true;
                    break;
                }
            }

            if (isBlacklist && matched) {
                return true;
            }
            if (!isBlacklist && !matched) {
                return true;
            }
        }
        return false;
    }

    private boolean hasNewOrUpgradedEnchantments(@Nullable ItemStack original, @Nullable ItemStack result) {
        if (result == null) {
            return false;
        }
        final Map<Enchantment, Integer> originalEnchants = (original != null) ? original.getEnchantments() : Map.of();
        final Map<Enchantment, Integer> resultEnchants = result.getEnchantments();

        for (Map.Entry<Enchantment, Integer> entry : resultEnchants.entrySet()) {
            final Enchantment enchant = entry.getKey();
            final int resultLevel = entry.getValue();
            final int originalLevel = originalEnchants.getOrDefault(enchant, 0);

            if (resultLevel > originalLevel) {
                return true;
            }
        }
        return false;
    }

    public static int countGlobalMaces(@NotNull Server server) {
        int total = 0;
        for (Player player : server.getOnlinePlayers()) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == Material.MACE) {
                    total += item.getAmount();
                }
            }
            for (ItemStack item : player.getEnderChest().getContents()) {
                if (item != null && item.getType() == Material.MACE) {
                    total += item.getAmount();
                }
            }
        }
        for (World world : server.getWorlds()) {
            for (Item itemEntity : world.getEntitiesByClass(Item.class)) {
                final ItemStack stack = itemEntity.getItemStack();
                if (stack.getType() == Material.MACE) {
                    total += stack.getAmount();
                }
            }
        }
        return total;
    }

    public static void removeMacesFromPlayer(@NotNull Player player) {
        boolean modified = false;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack != null && stack.getType() == Material.MACE) {
                player.getInventory().setItem(i, null);
                modified = true;
            }
        }
        final ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType() == Material.MACE) {
            player.setItemOnCursor(null);
            modified = true;
        }
        if (modified) {
            player.updateInventory();
        }
    }

    public static void enforceExcessMaceLimit(@NotNull Player player, int maxLimit) {
        final int currentGlobal = countGlobalMaces(player.getServer());
        if (currentGlobal <= maxLimit) {
            return;
        }
        int excess = currentGlobal - maxLimit;
        final ItemStack cursor = player.getItemOnCursor();
        if (cursor.getType() == Material.MACE) {
            final int remove = Math.min(excess, cursor.getAmount());
            if (remove >= cursor.getAmount()) {
                player.setItemOnCursor(null);
            } else {
                cursor.setAmount(cursor.getAmount() - remove);
            }
            excess -= remove;
        }
        if (excess > 0) {
            for (int i = 0; i < player.getInventory().getSize(); i++) {
                final ItemStack stack = player.getInventory().getItem(i);
                if (stack != null && stack.getType() == Material.MACE) {
                    final int remove = Math.min(excess, stack.getAmount());
                    if (remove >= stack.getAmount()) {
                        player.getInventory().setItem(i, null);
                    } else {
                        stack.setAmount(stack.getAmount() - remove);
                    }
                    excess -= remove;
                    if (excess <= 0) {
                        break;
                    }
                }
            }
        }
        player.updateInventory();
    }
}
