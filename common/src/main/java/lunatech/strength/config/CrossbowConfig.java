package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Crossbow weapon abilities (Passive Damage Multiplier & Ultimate Tranquilizer Immobilization).
 */
@ConfigSerializable
public class CrossbowConfig implements VersionedConfig {
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

    @Comment("Master toggle for Crossbow weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Crossbow Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Crossbow Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Crossbow passive ability.")
        public boolean enabled = true;

        @Comment("Number of valid crossbow arrow hits required to trigger passive damage multiplier")
        public int hitsRequired = 3;

        @Comment("Damage multiplier applied when passive is triggered")
        public double damageMultiplier = 2.0;

        @Comment("Message sent when crossbow passive 3rd shot deals 2x damage")
        public String passiveTriggeredShooterMessage = "<color:#ff0000>  <white>Crossbow Passive triggered!</color>";

        @Comment("Enable or disable slowness infliction when shooting a running player facing away")
        public boolean enableSlownessOnFleeing = true;

        @Comment("Duration of slowness effect in seconds applied to fleeing targets")
        public int slownessDurationSeconds = 3;

        @Comment("Amplifier level of slowness effect (0 = Slowness I, 1 = Slowness II)")
        public int slownessAmplifier = 0;

        @Comment("Message sent when slowness is applied to a fleeing target")
        public String slownessAppliedMessage = "";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Crossbow ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate")
        public int strengthRequired = 5;

        @Comment("Crossbow passive 3rd-shot triggers required to charge Ultimate")
        public int hitsRequired = 0;

        @Comment("Duration of the tranquilizer immobilization in seconds")
        public int immobilizeDurationSeconds = 3;

        @Comment("Cooldown in seconds before Crossbow Ultimate can be used again")
        public int cooldownSeconds = 60;

        @Comment("Message sent when crossbow ultimate is disabled")
        public String ultimateDisabledMessage = "<red>Crossbow ultimate ability is currently disabled!</red>";

        @Comment("Message sent when crossbow ultimate is fully charged")
        public String ultimateChargedMessage = "<green><bold>Crossbow Ultimate is fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> passive hits</gray>";

        @Comment("Message sent when crossbow ultimate is activated")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Crossbow Ultimate Activated! Your next shot will immobilize enemy!</color>";

        @Comment("Message sent to the shooter when ultimate hits an enemy")
        public String immobilizedShooterMessage = "<color:#ff0000>  <white>Crossbow Ultimate Hit! Target immobilized</color>";

        @Comment("Message sent to the victim when hit by ultimate shot")
        public String immobilizedVictimMessage = "";

        @Comment("Message sent when Crossbow Ultimate is on cooldown")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent when player is not holding a Crossbow")
        public String mustHoldCrossbowMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding a Crossbow to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is not charged yet")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> passive hits)</red>";

        @Comment("Message sent to the immobilized player when attempting to move or teleport")
        public String trapEscapeBlockedMessage = "";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
