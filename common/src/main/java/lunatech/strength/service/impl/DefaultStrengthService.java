package lunatech.strength.service.impl;

import lunatech.strength.constant.PDCKeys;
import lunatech.strength.config.ConfigHandler;
import lunatech.strength.config.PluginConfig.StrengthSettings;
import lunatech.strength.config.PluginConfig.WithdrawItemSettings;
import lunatech.strength.data.model.PlayerData;
import lunatech.strength.data.repository.PlayerRepository;
import lunatech.strength.service.StrengthService;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the StrengthService.
 */
public final class DefaultStrengthService implements StrengthService {
    private final PlayerRepository playerRepository;
    private final ConfigHandler configHandler;

    public DefaultStrengthService(@NotNull PlayerRepository playerRepository, @NotNull ConfigHandler configHandler) {
        this.playerRepository = playerRepository;
        this.configHandler = configHandler;
    }

    @Override
    public int getStrength(@NotNull Player player) {
        final StrengthSettings settings = configHandler.getConfig().strength;
        return playerRepository.get(player)
            .map(PlayerData::strength)
            .orElse(settings.defaultStrength);
    }

    @Override
    public void setStrength(@NotNull Player player, int strength) {
        final StrengthSettings settings = configHandler.getConfig().strength;
        // Clamp the strength value within the config boundaries
        final int clampedStrength = Math.max(settings.minStrength, Math.min(settings.maxStrength, strength));
        final String assignedWeapon = getAssignedWeapon(player);
        
        playerRepository.save(player, new PlayerData(clampedStrength, assignedWeapon));
        applyAttributeModifier(player, clampedStrength);
    }

    @Override
    public @Nullable String getAssignedWeapon(@NotNull Player player) {
        return playerRepository.get(player)
            .map(PlayerData::assignedWeapon)
            .orElse(null);
    }

    @Override
    public void setAssignedWeapon(@NotNull Player player, @Nullable String weapon) {
        final int currentStrength = getStrength(player);
        playerRepository.save(player, new PlayerData(currentStrength, weapon));
    }

    @Override
    public void applyAttributeModifier(@NotNull Player player, int strength) {
        final AttributeInstance ai = player.getAttribute(Attribute.ATTACK_DAMAGE);
        if (ai != null) {
            ai.removeModifier(PDCKeys.STRENGTH);
            if (strength > 0) {
                final AttributeModifier modifier = new AttributeModifier(
                    PDCKeys.STRENGTH,
                    strength,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ANY
                );
                ai.addModifier(modifier);
            }
        }
    }

    @Override
    public @NotNull ItemStack createStrengthItem(int amount) {
        final StrengthSettings settings = configHandler.getConfig().strength;
        final WithdrawItemSettings itemSettings = settings.withdrawItem;

        Material material = Material.matchMaterial(itemSettings.material);
        if (material == null) {
            material = Material.NAUTILUS_SHELL;
        }

        final ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            // Set Display Name
            meta.displayName(MiniMessage.miniMessage().deserialize(itemSettings.displayName).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

            // Set Lore
            final List<Component> loreComponents = new ArrayList<>();
            for (String line : itemSettings.lore) {
                loreComponents.add(MiniMessage.miniMessage().deserialize(line.replace("<amount>", String.valueOf(amount))).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            }
            meta.lore(loreComponents);

            // Set Custom Model Data
            meta.setCustomModelData(itemSettings.customModelData);

            // Save strength value in PDC
            meta.getPersistentDataContainer().set(PDCKeys.ITEM_STRENGTH, PersistentDataType.INTEGER, amount);
        });

        return item;
    }

    @Override
    public @NotNull ItemStack createRerollItem() {
        final lunatech.strength.config.PluginConfig.RerollItemSettings itemSettings = configHandler.getConfig().rerollItem;

        Material material = Material.matchMaterial(itemSettings.material);
        if (material == null) {
            material = Material.BOOK;
        }

        final ItemStack item = new ItemStack(material);
        item.editMeta(meta -> {
            meta.displayName(MiniMessage.miniMessage().deserialize(itemSettings.displayName).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));

            final List<Component> loreComponents = new ArrayList<>();
            for (String line : itemSettings.lore) {
                loreComponents.add(MiniMessage.miniMessage().deserialize(line).decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            }
            meta.lore(loreComponents);

            meta.setCustomModelData(itemSettings.customModelData);

            meta.getPersistentDataContainer().set(PDCKeys.ITEM_REROLL, PersistentDataType.INTEGER, 1);
        });

        return item;
    }
}
