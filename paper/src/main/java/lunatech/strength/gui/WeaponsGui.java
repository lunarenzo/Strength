package lunatech.strength.gui;

import lunatech.strength.Strength;
import lunatech.strength.config.WeaponsGuiConfig;
import lunatech.strength.config.WeaponsGuiConfig.GuiItemConfig;
import lunatech.strength.config.WeaponsGuiConfig.GuiSlotItemConfig;
import lunatech.strength.utility.ItemResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Manager responsible for opening the Strength Weapons Chest GUI (/strength weapons).
 */
public final class WeaponsGui {

    private WeaponsGui() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void open(@NotNull Strength plugin, @NotNull Player player) {
        final WeaponsGuiConfig guiConfig = plugin.getConfigHandler().getWeaponsGuiConfig();
        final MiniMessage mm = MiniMessage.miniMessage();

        final Component title = mm.deserialize(guiConfig.title);
        final int rows = Math.max(1, Math.min(6, guiConfig.rows));
        final int totalSlots = rows * 9;

        final WeaponsGuiHolder holder = new WeaponsGuiHolder();
        final Inventory inv = Bukkit.createInventory(holder, totalSlots, title);
        holder.setInventory(inv);

        final int playerStrength = plugin.getStrengthService().getStrength(player);
        final String assignedWeapon = plugin.getStrengthService().getAssignedWeapon(player);
        final String formattedWeapon = assignedWeapon != null && !assignedWeapon.trim().isEmpty() ? assignedWeapon.toUpperCase() : "NONE";

        // 1. Fill background items
        final GuiItemConfig fillerConfig = guiConfig.filler;
        ItemStack fillerItem = ItemResolver.resolveItemStack(fillerConfig.material, plugin.getStrengthService());
        if (fillerItem == null) {
            fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        fillerItem.editMeta(meta -> {
            if (fillerConfig.customModelData > 0) {
                meta.setCustomModelData(fillerConfig.customModelData);
            }
            if (fillerConfig.enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            if (fillerConfig.displayName != null && !fillerConfig.displayName.isEmpty()) {
                meta.displayName(mm.deserialize(fillerConfig.displayName).decoration(TextDecoration.ITALIC, false));
            } else {
                meta.displayName(Component.empty());
            }
        });

        for (int i = 0; i < totalSlots; i++) {
            inv.setItem(i, fillerItem.clone());
        }

        // 2. Player Profile Skull Item (Row 1)
        final GuiSlotItemConfig skullConfig = guiConfig.playerSkull;
        if (skullConfig.slot >= 0 && skullConfig.slot < totalSlots) {
            ItemStack skullItem = ItemResolver.resolveItemStack(skullConfig.material, plugin.getStrengthService());
            if (skullItem == null || skullItem.getType() != Material.PLAYER_HEAD) {
                skullItem = new ItemStack(Material.PLAYER_HEAD);
            }

            skullItem.editMeta(meta -> {
                if (meta instanceof SkullMeta skullMeta) {
                    skullMeta.setOwningPlayer(player);
                }
                if (skullConfig.customModelData > 0) {
                    meta.setCustomModelData(skullConfig.customModelData);
                }
                if (skullConfig.enchanted) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                final String rawName = replacePlaceholders(skullConfig.displayName, player.getName(), playerStrength, formattedWeapon, "");
                meta.displayName(mm.deserialize(rawName).decoration(TextDecoration.ITALIC, false));

                if (skullConfig.lore != null && !skullConfig.lore.isEmpty()) {
                    final List<Component> loreList = new ArrayList<>(skullConfig.lore.size());
                    for (String line : skullConfig.lore) {
                        final String replaced = replacePlaceholders(line, player.getName(), playerStrength, formattedWeapon, "");
                        loreList.add(mm.deserialize(replaced).decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(loreList);
                }
            });

            inv.setItem(skullConfig.slot, skullItem);
        }

        // 3. Weapon items
        for (Map.Entry<String, GuiSlotItemConfig> entry : guiConfig.weapons.entrySet()) {
            final String weaponKey = entry.getKey();
            final GuiSlotItemConfig weaponCfg = entry.getValue();

            if (weaponCfg.slot < 0 || weaponCfg.slot >= totalSlots) {
                continue;
            }

            ItemStack weaponItem = ItemResolver.resolveItemStack(weaponCfg.material, plugin.getStrengthService());
            if (weaponItem == null) {
                weaponItem = new ItemStack(Material.STONE);
            }

            final boolean isAssigned = formattedWeapon.equalsIgnoreCase(weaponKey);
            final String statusStr = isAssigned
                ? "<green><bold>ASSIGNED</bold></green>"
                : "<gray>Unassigned</gray>";

            weaponItem.editMeta(meta -> {
                if (weaponCfg.customModelData > 0) {
                    meta.setCustomModelData(weaponCfg.customModelData);
                }

                if (weaponCfg.enchanted || isAssigned) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                final String rawName = replacePlaceholders(weaponCfg.displayName, player.getName(), playerStrength, formattedWeapon, statusStr);
                meta.displayName(mm.deserialize(rawName).decoration(TextDecoration.ITALIC, false));

                if (weaponCfg.lore != null && !weaponCfg.lore.isEmpty()) {
                    final List<Component> loreList = new ArrayList<>(weaponCfg.lore.size());
                    for (String line : weaponCfg.lore) {
                        final String replaced = replacePlaceholders(line, player.getName(), playerStrength, formattedWeapon, statusStr);
                        loreList.add(mm.deserialize(replaced).decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(loreList);
                }
            });

            inv.setItem(weaponCfg.slot, weaponItem);
        }

        // 4. Strength Item
        final GuiSlotItemConfig strengthConfig = guiConfig.strengthItem;
        if (strengthConfig != null && strengthConfig.slot >= 0 && strengthConfig.slot < totalSlots) {
            ItemStack strengthItem = ItemResolver.resolveItemStack(strengthConfig.material, plugin.getStrengthService());
            if (strengthItem == null) {
                strengthItem = plugin.getStrengthService().createStrengthItem(1);
            }

            strengthItem.editMeta(meta -> {
                if (strengthConfig.customModelData > 0) {
                    meta.setCustomModelData(strengthConfig.customModelData);
                }
                if (strengthConfig.enchanted) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                final String rawName = replacePlaceholders(strengthConfig.displayName, player.getName(), playerStrength, formattedWeapon, "");
                meta.displayName(mm.deserialize(rawName).decoration(TextDecoration.ITALIC, false));

                if (strengthConfig.lore != null && !strengthConfig.lore.isEmpty()) {
                    final List<Component> loreList = new ArrayList<>(strengthConfig.lore.size());
                    for (String line : strengthConfig.lore) {
                        final String replaced = replacePlaceholders(line, player.getName(), playerStrength, formattedWeapon, "");
                        loreList.add(mm.deserialize(replaced).decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(loreList);
                }
            });

            inv.setItem(strengthConfig.slot, strengthItem);
        }

        // 5. Reroll Book Item
        final GuiSlotItemConfig rerollConfig = guiConfig.rerollItem;
        if (rerollConfig != null && rerollConfig.slot >= 0 && rerollConfig.slot < totalSlots) {
            ItemStack rerollItem = ItemResolver.resolveItemStack(rerollConfig.material, plugin.getStrengthService());
            if (rerollItem == null) {
                rerollItem = plugin.getStrengthService().createRerollItem();
            }

            rerollItem.editMeta(meta -> {
                if (rerollConfig.customModelData > 0) {
                    meta.setCustomModelData(rerollConfig.customModelData);
                }
                if (rerollConfig.enchanted) {
                    meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }

                final String rawName = replacePlaceholders(rerollConfig.displayName, player.getName(), playerStrength, formattedWeapon, "");
                meta.displayName(mm.deserialize(rawName).decoration(TextDecoration.ITALIC, false));

                if (rerollConfig.lore != null && !rerollConfig.lore.isEmpty()) {
                    final List<Component> loreList = new ArrayList<>(rerollConfig.lore.size());
                    for (String line : rerollConfig.lore) {
                        final String replaced = replacePlaceholders(line, player.getName(), playerStrength, formattedWeapon, "");
                        loreList.add(mm.deserialize(replaced).decoration(TextDecoration.ITALIC, false));
                    }
                    meta.lore(loreList);
                }
            });

            inv.setItem(rerollConfig.slot, rerollItem);
        }

        player.openInventory(inv);
    }

    private static String replacePlaceholders(String text, String playerName, int strength, String weapon, String status) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("{player}", playerName)
            .replace("{strength}", String.valueOf(strength))
            .replace("{weapon}", weapon)
            .replace("{status}", status);
    }
}
