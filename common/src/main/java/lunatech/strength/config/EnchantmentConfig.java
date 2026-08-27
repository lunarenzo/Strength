package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

/**
 * Configuration file for Global Enchantment Restrictions (`enchantment-restrictions.yml`).
 * Enables server owners to limit or blacklist specific enchantments and max levels on weapons, armors, and tools globally.
 */
@ConfigSerializable
public class EnchantmentConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Enable or disable global enchantment restriction submodule")
    public boolean enabled = true;

    @Comment("Restriction mode: 'BLACKLIST' (blocks blacklisted enchantments or levels exceeding max-levels) or 'WHITELIST' (only allows explicitly whitelisted enchantments)")
    public String mode = "BLACKLIST";

    @Comment("Global maximum allowed level per enchantment key (e.g. sharpness: 4, protection: 4). If an enchantment level exceeds this value, it is automatically capped.")
    public Map<String, Integer> maxLevels = Map.of(
        "sharpness", 4,
        "protection", 4,
        "power", 4,
        "efficiency", 5
    );

    @Comment("List of forbidden enchantment keys (e.g. mending, knockback). If present on an item, the enchantment is removed.")
    public List<String> blacklistedEnchantments = List.of(
        "mending"
    );

    @Comment("List of allowed enchantment keys when mode is WHITELIST (all other enchantments will be removed).")
    public List<String> whitelistedEnchantments = List.of(
        "sharpness", "unbreaking", "protection", "feather_falling", "efficiency", "fortune"
    );

    @Comment("Sanitize enchantments on items in container loot generation (chests, fishing, trial chambers)")
    public boolean blockInLoot = true;

    @Comment("Sanitize enchantments on villager trade offers (Librarians, Armorers, Weaponsmiths)")
    public boolean blockInTrades = true;

    @Comment("Sanitize enchantments when players pick up items from the ground")
    public boolean blockOnPickup = true;
}
