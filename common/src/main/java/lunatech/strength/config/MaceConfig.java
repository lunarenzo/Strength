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
 * Decoupled configuration for Mace weapon abilities (Passive Smash Cooldown Reduction & Ultimate Auto-Enchant Zero Cooldown).
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

    @Comment("Master toggle for Mace weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Mace Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Mace Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Mace passive smash cooldown reduction.")
        public boolean enabled = true;

        @Comment("Percentage of Mace Smash cooldown reduced for Mace-assigned players (e.g. 50.0 = 50% reduction).")
        public double cooldownReductionPercent = 50.0;

        @Comment("Message sent when Mace passive smash cooldown reduction applies.")
        public String passiveTriggeredMessage = "<gold><bold>MACE PASSIVE!</bold> Smash attack cooldown reduced by <percent>%!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Mace ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Mace Ultimate.")
        public int strengthRequired = 5;

        @Comment("Ultimate active duration in seconds.")
        public int durationSeconds = 15;

        @Comment("Cooldown in seconds for Mace Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("List of enchantments automatically applied to Mace during ultimate (Format: ENCHANTMENT_KEY:LEVEL).")
        public List<String> autoEnchantments = List.of("WIND_BURST:2", "DENSITY:5", "BREACH:4");

        @Comment("Message sent when Mace ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>MACE ULTIMATE ACTIVATED!</bold> Auto-enchanted & zero smash cooldown for <duration>s!</gold>";

        @Comment("Message sent when Mace ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Mace Ultimate has expired.</red>";

        @Comment("Message sent when player is not holding a Mace.")
        public String mustHoldMaceMessage = "<red>You must be holding a Mace to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<gold><bold>⚡ ULTIMATE READY!</bold> Your <yellow>{weapon}</yellow> ultimate is ready to use! Type <yellow>/ability</yellow>!</gold>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "<gold><bold>⚡ {weapon} ULTIMATE READY!</bold></gold>";
    }
}
