package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.time.Duration;

/**
 * Configuration file for Modular Server Rules (`rules.yml`).
 * Allows server owners to enforce gameplay rules such as Totem of Undying limits and persisted cooldowns.
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
        public java.util.List<String> gearKeywords = java.util.List.of(
            "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS",
            "SWORD", "AXE", "BOW", "CROSSBOW", "TRIDENT", "MACE", "SHIELD"
        );

        @Comment("Message sent to killer when strength reward is denied because the victim was naked")
        public String nakedKillNoRewardMessage = "<red>You did not receive strength for killing <victim> because they were naked!</red>";
    }
}
