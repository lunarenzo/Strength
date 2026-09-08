package lunatech.strength.listener.player;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import lunatech.strength.Strength;
import lunatech.strength.config.SpearConfig;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener handling Spear passive abilities (faster attack speed, bonus poke damage, hit charging),
 * zero hunger drain on lunge during ultimate, and exploit-proof temporary enchantment management.
 */
public final class SpearAbilityListener implements Listener {
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> activeUltimatePlayers = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();

    private static final NamespacedKey TEMP_ULT_KEY = new NamespacedKey("strengthsmp", "spear_ult_temp");
    private static final NamespacedKey ATTACK_SPEED_KEY = new NamespacedKey("strengthsmp", "spear_attack_speed");

    private final Strength plugin;
    private final StrengthService strengthService;

    public SpearAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    public static boolean isSpear(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        final String matName = item.getType().name();
        return matName.endsWith("_SPEAR") || "SPEAR".equals(matName);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpearDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player) || !(event.getEntity() instanceof LivingEntity)) {
            return;
        }

        final SpearConfig config = plugin.getConfigHandler().getSpearConfig();
        if (config == null || !config.enabled) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(player);
        if (assigned == null || !"spear".equalsIgnoreCase(assigned)) {
            return;
        }

        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!isSpear(mainHand)) {
            return;
        }

        final UUID uuid = player.getUniqueId();

        // 1. Passive Bonus Poke Damage
        if (config.passive.enabled && config.passive.bonusPokeDamage > 0.0) {
            event.setDamage(event.getDamage() + config.passive.bonusPokeDamage);

            if (config.passive.passiveTriggeredMessage != null && !config.passive.passiveTriggeredMessage.isBlank()) {
                final String msg = config.passive.passiveTriggeredMessage
                    .replace("<damage>", String.valueOf(config.passive.bonusPokeDamage))
                    .replace("{damage}", String.valueOf(config.passive.bonusPokeDamage));
                player.sendActionBar(ColorParser.of(msg).build());
            }
        }

        // 2. Ultimate Hit Charging
        if (config.ultimate.enabled) {
            final int currentHits = ultimateHits.getOrDefault(uuid, 0);
            if (currentHits < config.ultimate.hitsRequired) {
                final int newHits = currentHits + 1;
                ultimateHits.put(uuid, newHits);

                if (newHits == config.ultimate.hitsRequired) {
                    player.sendActionBar(ColorParser.of("<gold><bold>SPEAR ULTIMATE READY!</bold> Type /ability to activate!</gold>").build());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExhaustion(EntityExhaustionEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        if (!activeUltimatePlayers.containsKey(uuid)) {
            return;
        }

        final SpearConfig config = plugin.getConfigHandler().getSpearConfig();
        if (config != null && config.enabled && config.ultimate.enabled && config.ultimate.noHungerOnLunge) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final String assigned = strengthService.getAssignedWeapon(player);
        final boolean isSpearUser = assigned != null && "spear".equalsIgnoreCase(assigned);

        final ItemStack nextItem = player.getInventory().getItem(event.getNewSlot());
        final AttributeInstance speedAttr = player.getAttribute(Attribute.ATTACK_SPEED);

        if (speedAttr != null) {
            removeAttackSpeedModifier(speedAttr);
            if (isSpearUser && isSpear(nextItem)) {
                final SpearConfig config = plugin.getConfigHandler().getSpearConfig();
                if (config != null && config.enabled && config.passive.enabled && config.passive.matchSwordAttackSpeed) {
                    // Add +4.0 attack speed so spear recovery matches sword speed 1.6
                    speedAttr.addModifier(new AttributeModifier(ATTACK_SPEED_KEY, 4.0, AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        }
    }

    /* =========================================================================
     * Auto-Enchantment Management
     * ========================================================================= */

    public static void applyTemporaryEnchantments(@NotNull Player player, @NotNull SpearConfig.UltimateConfig settings) {
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!isSpear(mainHand)) return;

        final ItemMeta meta = mainHand.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
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

        mainHand.setItemMeta(meta);
    }

    public static void stripTemporaryEnchantments(@Nullable ItemStack item, @NotNull SpearConfig.UltimateConfig settings) {
        if (!isSpear(item)) return;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(TEMP_ULT_KEY, PersistentDataType.BYTE)) return;

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

    public static void stripTemporaryEnchantmentsFromPlayer(@NotNull Player player, @NotNull SpearConfig.UltimateConfig settings) {
        for (ItemStack item : player.getInventory().getContents()) {
            stripTemporaryEnchantments(item, settings);
        }
    }

    /* =========================================================================
     * Event Guards for Exploit Prevention & Cleanup
     * ========================================================================= */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        final SpearConfig settings = plugin.getConfigHandler().getSpearConfig();
        if (settings != null) {
            stripTemporaryEnchantments(droppedItem.getItemStack(), settings.ultimate);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        final SpearConfig settings = plugin.getConfigHandler().getSpearConfig();
        if (settings != null) {
            stripTemporaryEnchantments(event.getCurrentItem(), settings.ultimate);
            stripTemporaryEnchantments(event.getCursor(), settings.ultimate);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final UUID uuid = event.getEntity().getUniqueId();
        activeUltimatePlayers.remove(uuid);

        final SpearConfig settings = plugin.getConfigHandler().getSpearConfig();
        if (settings != null) {
            for (ItemStack drop : event.getDrops()) {
                stripTemporaryEnchantments(drop, settings.ultimate);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        activeUltimatePlayers.remove(uuid);
        ultimateCooldowns.remove(uuid);
        ultimateHits.remove(uuid);

        final AttributeInstance speedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (speedAttr != null) {
            removeAttackSpeedModifier(speedAttr);
        }

        final SpearConfig settings = plugin.getConfigHandler().getSpearConfig();
        if (settings != null) {
            stripTemporaryEnchantmentsFromPlayer(player, settings.ultimate);
        }
    }

    private void removeAttackSpeedModifier(AttributeInstance attributeInstance) {
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            if (ATTACK_SPEED_KEY.equals(modifier.getKey())) {
                attributeInstance.removeModifier(modifier);
            }
        }
    }
}
