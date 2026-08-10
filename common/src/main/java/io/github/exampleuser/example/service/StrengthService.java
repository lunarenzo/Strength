package io.github.exampleuser.example.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Service interface for managing Player Strength and applying the native Attribute Modifier.
 */
public interface StrengthService {
    /**
     * Retrieves the current strength level of a player.
     *
     * @param player the target player
     * @return the strength level
     */
    int getStrength(@NotNull Player player);

    /**
     * Updates the strength level of a player, saving it to PDC and updating attributes.
     *
     * @param player the target player
     * @param strength the new strength level
     */
    void setStrength(@NotNull Player player, int strength);

    /**
     * Synchronizes and applies the attack damage attribute modifier based on current strength.
     *
     * @param player the target player
     * @param strength the strength level to apply as modifier value
     */
    void applyAttributeModifier(@NotNull Player player, int strength);
}
