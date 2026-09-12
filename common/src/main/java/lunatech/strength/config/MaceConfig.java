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
        public String passiveTriggeredMessage = "<color:#ff0000>  <white>Mace Passive triggered!</color>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Mace ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Mace Ultimate.")
        public int strengthRequired = 5;

        @Comment("Ultimate active duration in seconds.")
        public int durationSeconds = 30;

        @Comment("Cooldown in seconds for Mace Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("List of enchantments automatically applied to Mace during ultimate (Format: ENCHANTMENT_KEY:LEVEL).")
        public List<String> autoEnchantments = List.of("WIND_BURST:2");

        @Comment("Message sent when Mace ultimate is activated.")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Mace Ultimate Activated!</color>";

        @Comment("Message sent when Mace ultimate expires.")
        public String ultimateExpiredMessage = "<color:#ff0000>  <white>Mace Ultimate Activated!</color>";

        @Comment("Message sent when player is not holding a Mace.")
        public String mustHoldMaceMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding a Mace to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
