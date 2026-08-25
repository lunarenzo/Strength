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

        @Comment("""
            ================================================================================
             MACE ENCHANTMENT RESTRICTION FORMAT GUIDE
            ================================================================================
             Configure list of enchantment names or namespaced keys to blacklist or whitelist.
             Supported Format Examples:
               - 'minecraft:density'
               - 'minecraft:breach'
               - 'minecraft:wind_burst'
               - 'minecraft:mending'
               - 'minecraft:unbreaking'
               - 'minecraft:sharpness'
               - 'minecraft:smite'
               - 'minecraft:bane_of_arthropods'
               - 'minecraft:fire_aspect'
               - 'minecraft:looting'
               - 'minecraft:knockback'
               - 'minecraft:curse_of_vanishing'
               - 'minecraft:curse_of_binding'
             You may enter either full namespaced keys (e.g. 'minecraft:density') or short names (e.g. 'density').
            ================================================================================
            """)
        public List<String> enchantments = List.of(
            "minecraft:density",
            "minecraft:breach",
            "minecraft:wind_burst"
        );
    }

    @ConfigSerializable
    public static class ContainerConfig {
        @Comment("Enable or disable mace container storage restriction submodule.")
        public boolean enabled = true;

        @Comment("Allow storing maces inside containers?")
        public boolean allowStorage = false;

        @Comment("Mode engine for container restrictions: BLACKLIST or WHITELIST.")
        public String mode = "BLACKLIST";

        @Comment("""
            Comprehensive default blacklist of all tile entities, storage items, container blocks, and transport storage entities.
            Covers: Chests, Trapped Chests, Ender Chests, Shulker Boxes (all 16 colors), Barrels, Furnaces, Blast Furnaces,
            Smokers, Hoppers, Droppers, Dispensers, Brewing Stands, Beacons, Crafters, Chiseled Bookshelves, Decorated Pots,
            Jukeboxes, Lecterns, Anvils, Bundles (all 16 colors), Storage Minecarts, Storage Boats, Rafts, Grindstones, Looms,
            Cartography Tables, Smithing Tables, Stonecutters, Workbenches, Composters, and Campfires.
            """)
        public List<String> containers = List.of(
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "SHULKER", "BARREL",
            "FURNACE", "BLAST_FURNACE", "SMOKER", "HOPPER", "DROPPER", "DISPENSER",
            "BREWING", "BEACON", "CRAFTER", "BOOKSHELF", "DECORATED_POT", "JUKEBOX",
            "LECTERN", "ANVIL", "BUNDLE", "MINECART", "BOAT", "RAFT", "GRINDSTONE",
            "LOOM", "CARTOGRAPHY", "SMITHING", "STONECUTTER", "WORKBENCH", "COMPOSTER", "CAMPFIRE"
        );
    }
}
