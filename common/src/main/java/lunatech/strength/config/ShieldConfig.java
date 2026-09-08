package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Shield weapon abilities (Passive Post-Disable Damage Reduction, Ultimate God Mode Bubble Barrier).
 */
@ConfigSerializable
public class ShieldConfig implements VersionedConfig {
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

    @Comment("Master toggle for Shield weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Shield Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Shield Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Shield passive damage reduction.")
        public boolean enabled = true;

        @Comment("Duration in seconds for passive damage reduction after shield is disabled or broken.")
        public int durationSeconds = 5;

        @Comment("Percentage of incoming damage to reduce during passive protection (e.g., 20.0 = 20% reduction).")
        public double damageReductionPercentage = 20.0;

        @Comment("Message sent when shield passive protection activates.")
        public String passiveActivatedMessage = "<gold><bold>SHIELD DISABLED!</bold> Gained <reduction>% damage reduction for <seconds> seconds!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Shield ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate.")
        public int strengthRequired = 5;

        @Comment("Shield blocks required to charge Ultimate.")
        public int hitsRequired = 10;

        @Comment("Cooldown in seconds for the Shield Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("The material of the ultimate bubble item display.")
        public String bubbleMaterial = "NAUTILUS_SHELL";

        @Comment("The custom model data of the ultimate bubble item display.")
        public int bubbleCustomModelData = 12346;

        @Comment("Ultimate duration in ticks (20 ticks = 1 second, default 15s = 300 ticks).")
        public int durationTicks = 300;

        @Comment("Ultimate bubble visual translation offset X.")
        public float bubbleOffsetX = 0.0f;

        @Comment("Ultimate bubble visual translation offset Y.")
        public float bubbleOffsetY = -0.5f;

        @Comment("Ultimate bubble visual translation offset Z.")
        public float bubbleOffsetZ = 0.0f;

        @Comment("Message sent when shield ultimate ability is disabled.")
        public String ultimateDisabledMessage = "<red>Shield ultimate ability is currently disabled!</red>";

        @Comment("Message sent when shield ultimate is fully charged.")
        public String ultimateChargedMessage = "<green><bold>Shield Ultimate is fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress.")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> blocks</gray>";

        @Comment("Message sent when shield ultimate is activated.")
        public String ultimateActivatedMessage = "<green><bold>SHIELD ULTIMATE ACTIVATED!</bold> Gained God Mode bubble barrier!</green>";

        @Comment("Message sent when shield ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Shield Ultimate bubble has expired!</red>";

        @Comment("Message sent when shield ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";

        @Comment("Message sent when player is not holding a Shield for ultimate.")
        public String mustHoldShieldMessage = "<red>You must be holding a Shield to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength for ultimate.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when player ultimate is not fully charged.")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> blocks)</red>";
    }
}
