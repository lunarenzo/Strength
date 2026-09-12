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
        public String passiveActivatedMessage = "<color:#ff0000> ⛨ <white>Shield Passive triggered!</color>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Shield ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate.")
        public int strengthRequired = 5;

        @Comment("Shield blocks required to charge Ultimate.")
        public int hitsRequired = 0;

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
        public float bubbleOffsetY = 0.10000000149011612f;

        @Comment("Ultimate bubble visual translation offset Z.")
        public float bubbleOffsetZ = 0.0f;

        @Comment("Message sent when shield ultimate ability is disabled.")
        public String ultimateDisabledMessage = "<red>Shield ultimate ability is currently disabled!</red>";

        @Comment("Message sent when shield ultimate is fully charged.")
        public String ultimateChargedMessage = "<green><bold>Shield Ultimate is fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress.")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> blocks</gray>";

        @Comment("Message sent when shield ultimate is activated.")
        public String ultimateActivatedMessage = "<color:#ff0000> ⛨ <white>Shield Ultimate Activated!</color>";

        @Comment("Message sent when shield ultimate expires.")
        public String ultimateExpiredMessage = "<color:#ff0000> ⛨ <white>Shield Ultimate Deactivated!</color>";

        @Comment("Message sent when shield ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<color:#ff0000> ⛨ <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent when player is not holding a Shield for ultimate.")
        public String mustHoldShieldMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding a Shield to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength for ultimate.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when player ultimate is not fully charged.")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> blocks)</red>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000> ⛨ <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
