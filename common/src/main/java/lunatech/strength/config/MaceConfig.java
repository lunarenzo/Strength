package lunatech.strength.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;

import java.util.List;
import java.util.Map;

/**
 * Decoupled configuration for Mace features and restrictions.
 */
@ConfigSerializable
public class MaceConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of();
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
    }

    @Comment("Master toggle for the entire mace feature module. Disabling this completely disables all mace features & restrictions.")
    public boolean enabled = true;

    @Comment("Mace Limit Submodule Settings")
    public LimitConfig limit = new LimitConfig();

    @Comment("Mace Cooldown Submodule Settings")
    public CooldownConfig cooldown = new CooldownConfig();

    @Comment("Mace Enchanting Submodule Settings")
    public EnchantConfig enchant = new EnchantConfig();

    @Comment("Mace Container Storage Restriction Submodule Settings")
    public ContainerConfig container = new ContainerConfig();

    @ConfigSerializable
    public static class LimitConfig {
        @Comment("Enable or disable the mace limit submodule.")
        public boolean enabled = true;

        @Comment("Maximum number of maces allowed globally across the server. Set to 0 to completely disable maces (crafting, holding, usage).")
        public int maxAmount = 3;
    }

    @ConfigSerializable
    public static class CooldownConfig {
        @Comment("Enable or disable mace smash attack cooldown submodule.")
        public boolean enabled = true;

        @Comment("Cooldown duration in seconds after performing a mace smash attack.")
        public int cooldownSeconds = 30;
    }

    @ConfigSerializable
    public static class EnchantConfig {
        @Comment("Enable or disable mace enchanting submodule.")
        public boolean enabled = true;

        @Comment("Allow maces to be enchanted at all? If false, putting maces in enchanting tables/anvils or enchanting them is completely disabled.")
        public boolean allowEnchanting = false;

        @Comment("Mode engine for enchantment restrictions: WHITELIST or BLACKLIST.")
        public String mode = "BLACKLIST";

        @Comment("List of enchantment keys (e.g. 'minecraft:density', 'minecraft:breach', 'minecraft:wind_burst', 'minecraft:mending', etc.)")
        public List<String> enchantments = List.of();
    }

    @ConfigSerializable
    public static class ContainerConfig {
        @Comment("Enable or disable mace container storage restriction submodule.")
        public boolean enabled = true;

        @Comment("Allow storing maces inside containers?")
        public boolean allowStorage = false;

        @Comment("Mode engine for container restrictions: BLACKLIST or WHITELIST.")
        public String mode = "BLACKLIST";

        @Comment("List of container materials or inventory types (e.g. 'CHEST', 'TRAPPED_CHEST', 'ENDER_CHEST', 'SHULKER_BOX', 'BARREL', 'FURNACE', 'BLAST_FURNACE', 'SMOKER', 'HOPPER', 'DROPPER', 'DISPENSER', 'BREWING_STAND', 'BUNDLE', etc.)")
        public List<String> containers = List.of(
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "SHULKER_BOX", "BARREL",
            "FURNACE", "BLAST_FURNACE", "SMOKER", "HOPPER", "DROPPER", "DISPENSER",
            "BREWING_STAND", "BUNDLE"
        );
    }
}
