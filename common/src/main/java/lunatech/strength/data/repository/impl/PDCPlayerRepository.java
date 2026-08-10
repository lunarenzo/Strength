package lunatech.strength.data.repository.impl;

import lunatech.strength.constant.PDCKeys;
import lunatech.strength.data.model.PlayerData;
import lunatech.strength.data.repository.PlayerRepository;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * PersistentDataContainer (PDC) implementation of PlayerRepository.
 * Stores player states inside entity NBT metadata.
 */
public final class PDCPlayerRepository implements PlayerRepository {

    @Override
    public @NotNull Optional<PlayerData> get(@NotNull Player player) {
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (!pdc.has(PDCKeys.STRENGTH, PersistentDataType.INTEGER)) {
            return Optional.empty();
        }
        final int strength = pdc.getOrDefault(PDCKeys.STRENGTH, PersistentDataType.INTEGER, 0);
        final String assignedWeapon = pdc.get(PDCKeys.ASSIGNED_WEAPON, PersistentDataType.STRING);
        return Optional.of(new PlayerData(strength, assignedWeapon));
    }

    @Override
    public void save(@NotNull Player player, @NotNull PlayerData data) {
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        pdc.set(PDCKeys.STRENGTH, PersistentDataType.INTEGER, data.strength());
        if (data.assignedWeapon() != null) {
            pdc.set(PDCKeys.ASSIGNED_WEAPON, PersistentDataType.STRING, data.assignedWeapon());
        } else {
            pdc.remove(PDCKeys.ASSIGNED_WEAPON);
        }
    }
}
