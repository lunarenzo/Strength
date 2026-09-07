package lunatech.strength.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;

import java.util.Map;

/**
 * Decoupled configuration for Mace weapon abilities (Passive & Ultimate).
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
        @Comment("Enable or disable Mace passive ability.")
        public boolean enabled = true;

        @Comment("Hits required using Mace to trigger passive shockwave.")
        public int hitsRequired = 3;

        @Comment("Damage multiplier applied on the passive shockwave hit.")
        public double damageMultiplier = 1.5;

        @Comment("AoE shockwave radius in blocks around the target.")
        public double shockwaveRadius = 3.5;

        @Comment("Message sent when Mace passive is triggered.")
        public String passiveTriggeredMessage = "<gold><bold>MACE PASSIVE!</bold> Heavy Seismic Shockwave triggered!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Mace Cataclysmic Slam ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Mace Ultimate.")
        public int strengthRequired = 5;

        @Comment("Hits required using Mace to charge Ultimate.")
        public int hitsRequired = 5;

        @Comment("Cooldown in seconds for Mace Ultimate.")
        public int cooldownSeconds = 45;

        @Comment("Leap upward velocity when activating Cataclysmic Slam.")
        public double leapVelocity = 1.2;

        @Comment("Slam explosion radius in blocks when landing.")
        public double slamRadius = 5.0;

        @Comment("Base damage dealt by the Cataclysmic Slam explosion.")
        public double slamDamage = 12.0;

        @Comment("Message sent when Mace ultimate is fully charged.")
        public String ultimateChargedMessage = "<green><bold>Cataclysmic Slam is fully charged! Type /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress.")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold> hits</gray>";

        @Comment("Message sent when Mace ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>CATACLYSMIC SLAM ACTIVATED!</bold> Leaped into the air — slam down!</gold>";

        @Comment("Message sent when player is not holding a Mace.")
        public String mustHoldMaceMessage = "<red>You must be holding a Mace to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is not charged yet.")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";
    }
}
