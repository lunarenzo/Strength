package lunatech.strength.data.repository;

import lunatech.strength.data.model.PlayerData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Interface representing player data repository contracts.
 */
public interface PlayerRepository {
    /**
     * Retrieves the data associated with a player.
     *
     * @param player the target player
     * @return an optional containing the PlayerData, or empty if not found
     */
    @NotNull Optional<PlayerData> get(@NotNull Player player);

    /**
     * Saves the player data.
     *
     * @param player the target player
     * @param data the player data to persist
     */
    void save(@NotNull Player player, @NotNull PlayerData data);
}
