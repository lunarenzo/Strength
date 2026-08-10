package io.github.exampleuser.example.service.impl;

import io.github.exampleuser.example.constant.PDCKeys;
import io.github.exampleuser.example.config.ConfigHandler;
import io.github.exampleuser.example.config.PluginConfig.StrengthSettings;
import io.github.exampleuser.example.data.model.PlayerData;
import io.github.exampleuser.example.data.repository.PlayerRepository;
import io.github.exampleuser.example.service.StrengthService;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

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
}
