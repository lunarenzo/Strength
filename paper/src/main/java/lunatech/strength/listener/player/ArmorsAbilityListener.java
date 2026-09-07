package lunatech.strength.listener.player;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import lunatech.strength.Strength;
import lunatech.strength.config.ArmorsConfig;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener handling Armors/Gear Set passive auto-upgrade, automatic PDC reversion,
 * drop safety, and Golden Apple absorption synergy during Ultimate.
 */
public final class ArmorsAbilityListener implements Listener {
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Set<UUID> activeUltimatePlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Strength plugin;
    private final StrengthService strengthService;

    public ArmorsAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        final ArmorsConfig config = plugin.getConfigHandler().getArmorsConfig();
        if (config == null || !config.enabled || !config.passive.enabled) {
            return;
        }

        final Player player = event.getPlayer();
        final String assigned = strengthService.getAssignedWeapon(player);
        final boolean isArmors = assigned != null && ("armors".equalsIgnoreCase(assigned) || "armor".equalsIgnoreCase(assigned));

        final ItemStack newItem = event.getNewItem();
        final ItemStack oldItem = event.getOldItem();

        // 1. Process Unequipped Item (Old Item)
        if (oldItem != null && oldItem.getType() != Material.AIR) {
            revertUpgradedArmorPiece(oldItem);
        }

        // 2. Process Equipped Item (New Item)
        if (isArmors && newItem != null && newItem.getType() != Material.AIR) {
            upgradeArmorPieceIfEligible(player, event.getSlot(), newItem, config.passive);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent event) {
        final ArmorsConfig config = plugin.getConfigHandler().getArmorsConfig();
        if (config == null || !config.enabled || !config.ultimate.enabled) {
            return;
        }

        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (!activeUltimatePlayers.contains(uuid)) {
            return;
        }

        final ItemStack item = event.getItem();
        if (item.getType() == Material.GOLDEN_APPLE) {
            final int amp = config.ultimate.goldenAppleAbsorptionAmplifier;
            final int durationTicks = config.ultimate.goldenAppleAbsorptionDurationSeconds * 20;

            // Apply upgraded Golden Apple Absorption effect
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                player.removePotionEffect(PotionEffectType.ABSORPTION);
                player.addPotionEffect(new PotionEffect(
                    PotionEffectType.ABSORPTION,
                    durationTicks,
                    amp,
                    false,
                    true,
                    true
                ));
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDrop(PlayerDropItemEvent event) {
        final ItemStack dropped = event.getItemDrop().getItemStack();
        revertUpgradedArmorPiece(dropped);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final UUID uuid = event.getEntity().getUniqueId();
        activeUltimatePlayers.remove(uuid);

        for (ItemStack drop : event.getDrops()) {
            revertUpgradedArmorPiece(drop);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) {
            final ItemStack current = event.getCurrentItem();
            if (current != null && current.getType() != Material.AIR) {
                revertUpgradedArmorPiece(current);
            }
        }
        final ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() != Material.AIR) {
            revertUpgradedArmorPiece(cursor);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        activeUltimatePlayers.remove(event.getPlayer().getUniqueId());
    }

    private void upgradeArmorPieceIfEligible(Player player, EquipmentSlot slot, ItemStack item, ArmorsConfig.PassiveConfig passiveConfig) {
        final String baseMatName = item.getType().name();
        final String targetMatName = passiveConfig.upgrades.get(baseMatName);

        if (targetMatName == null) {
            return;
        }

        final Material targetMat = Material.matchMaterial(targetMatName);
        if (targetMat == null) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (pdc.has(PDCKeys.UPGRADED_GEAR, PersistentDataType.STRING)) {
            return; // Already upgraded
        }

        pdc.set(PDCKeys.UPGRADED_GEAR, PersistentDataType.STRING, baseMatName);
        item.setItemMeta(meta);
        item.setType(targetMat);

        final ItemStack upgradedItem = item.clone();

        // Update the player's equipment slot on next tick to update equipment container & client visuals/attributes
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.getEquipment().setItem(slot, upgradedItem);
            }
        });

        if (passiveConfig.armorUpgradedMessage != null && !passiveConfig.armorUpgradedMessage.isBlank()) {
            player.sendMessage(
                ColorParser.of(passiveConfig.armorUpgradedMessage
                    .replace("<old_armor>", formatMaterialName(baseMatName))
                    .replace("<new_armor>", formatMaterialName(targetMatName)))
                    .with("old_armor", formatMaterialName(baseMatName))
                    .with("new_armor", formatMaterialName(targetMatName))
                    .build()
            );
        }
    }

    public static void revertUpgradedArmorPiece(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        final String baseMatName = pdc.get(PDCKeys.UPGRADED_GEAR, PersistentDataType.STRING);

        if (baseMatName != null) {
            final Material baseMat = Material.matchMaterial(baseMatName);
            pdc.remove(PDCKeys.UPGRADED_GEAR);
            item.setItemMeta(meta);
            if (baseMat != null) {
                item.setType(baseMat);
            }
        }
    }

    public static boolean hasFullArmorSet(@NotNull Player player, @NotNull Map<String, String> upgrades) {
        final ItemStack[] armor = player.getInventory().getArmorContents();
        if (armor.length < 4) {
            return false;
        }

        for (ItemStack piece : armor) {
            if (piece == null || piece.getType() == Material.AIR) {
                return false;
            }
            final String matName = piece.getType().name();
            final boolean isBase = upgrades.containsKey(matName);
            final boolean isUpgraded = upgrades.containsValue(matName);
            if (!isBase && !isUpgraded) {
                return false;
            }
        }
        return true;
    }

    private static String formatMaterialName(String name) {
        final String[] parts = name.split("_");
        final StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(part.substring(0, 1).toUpperCase(Locale.ROOT))
                  .append(part.substring(1).toLowerCase(Locale.ROOT))
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
