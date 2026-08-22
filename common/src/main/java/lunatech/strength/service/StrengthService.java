package lunatech.strength.service;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Creates a physical strength item with the given strength value.
     *
     * @param amount the strength value stored in the item
     * @return the created ItemStack
     */
    @NotNull org.bukkit.inventory.ItemStack createStrengthItem(int amount);

    /**
     * Gets the assigned weapon of a player.
     *
     * @param player the target player
     * @return the assigned weapon name, or null if none
     */
    @Nullable String getAssignedWeapon(@NotNull Player player);

    /**
     * Sets the assigned weapon of a player.
     *
     * @param player the target player
     * @param weapon the assigned weapon name
     */
    void setAssignedWeapon(@NotNull Player player, @Nullable String weapon);

    /**
     * Creates a physical weapon reroll book item.
     *
     * @return the created ItemStack
     */
    @NotNull org.bukkit.inventory.ItemStack createRerollItem();
}
