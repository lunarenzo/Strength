package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.time.Duration;
import java.util.List;

/**
 * Configuration file for Modular Server Rules (`rules.yml`).
 * Allows server owners to enforce gameplay rules such as Totem of Undying limits, Naked Player anti-farming, and Mace restrictions.
 */
@ConfigSerializable
public class RulesConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Totem of Undying modular rules configuration")
    public TotemRules totem = new TotemRules();

    @Comment("Naked player (Anti-Strength Farming) rule configuration")
    public NakedPlayerRules nakedPlayer = new NakedPlayerRules();

    @Comment("Mace restriction rules configuration")
    public MaceRules mace = new MaceRules();

    @ConfigSerializable
    public static class TotemRules {
        @Comment("Enable or disable Totem of Undying rules module")
        public boolean enabled = true;

        @Comment("Mode: 'ALLOWED' (enforces maxInInventory limit & pop quota cooldown) or 'BANNED' (completely forbids Totem usage and holding)")
        public String mode = "ALLOWED";

        @Comment("Maximum number of Totems of Undying allowed in a player's inventory simultaneously (0 for unlimited)")
        public int maxInInventory = 3;

        @Comment("Maximum number of Totem pops allowed before triggering a cooldown")
        public int popQuota = 3;

        @Comment("If true, Totem pop quota is only consumed when the player is tagged in active combat via PvPManager. In PvE/survival, totems pop freely without using quota.")
        public boolean quotaOnlyInCombat = true;

        @Comment("Cooldown duration after consuming popQuota totems (e.g. 30m, 1h, 1d, 300s)")
        public Duration cooldownDuration = Duration.ofMinutes(30);

        @Comment("Prevent players from using Totems of Undying while tagged in active combat (requires PvPManager integration)")
        public boolean preventInCombat = false;

        @Comment("Message sent when a player tries to pick up or hold excess Totems beyond maxInInventory limit")
        public String maxLimitReachedMessage = "<red>You cannot hold more than <count> Totems of Undying in your inventory!</red>";

        @Comment("Message sent when a Totem pop is denied due to active combat")
        public String totemInCombatMessage = "<red>You cannot use or move Totems of Undying while in active combat!</red>";

        @Comment("Message sent when a Totem pop is denied due to active cooldown")
        public String totemOnCooldownMessage = "<red>Your Totem of Undying is on cooldown for another <time>!</red>";

        @Comment("Message sent when a player exhausts their totem quota and enters cooldown")
        public String quotaExhaustedMessage = "<red>You have exhausted your Totem quota! Totems disabled for <time>.</red>";
    }

    @ConfigSerializable
    public static class NakedPlayerRules {
        @Comment("Enable or disable Naked Player Anti-Farming rule")
        public boolean enabled = true;

        @Comment("Allow strength reward/item drops when killing a naked player (set false to prevent strength farming on naked alts/newbies)")
        public boolean allowNakedKillReward = false;

        @Comment("If true, a player with completely empty inventory slots (inventory, armor, offhand) is considered naked")
        public boolean requireCompletelyEmptyInventory = true;

        @Comment("If true, a player with no armor equipped AND no weapons in inventory is considered naked")
        public boolean checkArmorAndWeapons = true;

        @Comment("List of material names or wildcards considered armor or weapons for the check")
        public List<String> gearKeywords = List.of(
            "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS",
            "SWORD", "AXE", "BOW", "CROSSBOW", "TRIDENT", "MACE", "SHIELD"
        );

        @Comment("Message sent to killer when strength reward is denied because the victim was naked")
        public String nakedKillNoRewardMessage = "<red>You did not receive strength for killing <victim> because they were naked!</red>";
    }

    @ConfigSerializable
    public static class MaceRules {
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

            @Comment("Allow maces to be enchanted at all? If false, enchanting maces in enchanting tables or applying enchantments via anvils is disabled.")
            public boolean allowEnchanting = false;

            @Comment("Allow renaming maces in an anvil even when allowEnchanting is set to false?")
            public boolean allowRenaming = true;

            @Comment("Mode engine for enchantment restrictions: WHITELIST or BLACKLIST.")
            public String mode = "BLACKLIST";

            @Comment("Configure list of enchantment names or namespaced keys to blacklist or whitelist.")
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

            @Comment("List of tile entities, storage items, container blocks, and transport storage entities.")
            public List<String> containers = List.of(
                "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "SHULKER", "BARREL",
                "FURNACE", "BLAST_FURNACE", "SMOKER", "HOPPER", "DROPPER", "DISPENSER",
                "BREWING", "BEACON", "CRAFTER", "BOOKSHELF", "DECORATED_POT", "JUKEBOX",
                "LECTERN", "BUNDLE", "MINECART", "BOAT", "RAFT", "COMPOSTER", "CAMPFIRE"
            );
        }
    }
}
