package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

/**
 * Decoupled configuration for Crossbow2 weapon abilities (Passive Shield Piercing & Chance-based Flame, Ultimate Auto-Enchant Power V & Quick Charge V).
 */
@ConfigSerializable
public class Crossbow2Config implements VersionedConfig {
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

    @Comment("Master toggle for Crossbow2 weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Crossbow2 Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Crossbow2 Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Crossbow2 passive abilities.")
        public boolean enabled = true;

        @Comment("Bypass shield block on hit for Crossbow2 fired arrows.")
        public boolean shieldPiercing = true;

        @Comment("Percentage chance (0.0 - 100.0) to set target on fire when struck by a Crossbow2 arrow.")
        public double flameChance = 100.0;

        @Comment("Duration in seconds to set target on fire.")
        public int burnDurationSeconds = 5;

        @Comment("Message sent when Crossbow2 passive flame shot triggers.")
        public String passiveTriggeredMessage = "<gold><bold>FLAME SHOT!</bold> Target set on fire!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Crossbow2 ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Crossbow2 Ultimate.")
        public int strengthRequired = 5;

        @Comment("Ultimate active duration in seconds.")
        public int durationSeconds = 15;

        @Comment("Cooldown in seconds for Crossbow2 Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("List of enchantments automatically applied to Crossbow2 during ultimate (Format: ENCHANTMENT_KEY:LEVEL).")
        public List<String> autoEnchantments = List.of("POWER:5", "QUICK_CHARGE:5");

        @Comment("Message sent when Crossbow2 ultimate ability is disabled.")
        public String ultimateDisabledMessage = "<red>Crossbow2 ultimate ability is currently disabled!</red>";

        @Comment("Message sent when Crossbow2 ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>CROSSBOW2 ULTIMATE ACTIVATED!</bold> Power V & Quick Charge V enabled for <duration>s!</gold>";

        @Comment("Message sent when Crossbow2 ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Crossbow2 Ultimate has expired.</red>";

        @Comment("Message sent when player is not holding a Crossbow.")
        public String mustHoldCrossbowMessage = "<red>You must be holding a Crossbow to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";
    }
}
