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
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

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
        
        playerRepository.save(player, new PlayerData(clampedStrength));
        applyAttributeModifier(player, clampedStrength);
    }

    @Override
    public void applyAttributeModifier(@NotNull Player player, int strength) {
        final AttributeInstance ai = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
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
            meta.displayName(ColorParser.of(itemSettings.displayName).build());

            // Set Lore
            final List<Component> loreComponents = new ArrayList<>();
            for (String line : itemSettings.lore) {
                loreComponents.add(ColorParser.of(line.replace("<amount>", String.valueOf(amount))).build());
            }
            meta.lore(loreComponents);

            // Set Custom Model Data
            meta.setCustomModelData(itemSettings.customModelData);

            // Save strength value in PDC
            meta.getPersistentDataContainer().set(PDCKeys.ITEM_STRENGTH, PersistentDataType.INTEGER, amount);
        });

        return item;
    }
}
