package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class ShieldConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Duration in seconds for the passive damage reduction protection after shield is disabled or broken")
    public int passiveProtectionDurationSeconds = 5;

    @Comment("Percentage of incoming damage to reduce during passive protection (e.g., 20.0 = 20% reduction)")
    public double passiveDamageReductionPercentage = 20.0;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Shield blocks required to charge Ultimate")
    public int ultimateHitsRequired = 10;

    @Comment("Cooldown in seconds for the Shield Ultimate")
    public int ultimateCooldownSeconds = 60;

    @Comment("The material of the ultimate bubble item display")
    public String bubbleMaterial = "NAUTILUS_SHELL";

    @Comment("The custom model data of the ultimate bubble item display")
    public int bubbleCustomModelData = 12346;

    @Comment("Ultimate duration in ticks (20 ticks = 1 second, default 15s = 300 ticks)")
    public int ultimateDurationTicks = 300;

    @Comment("Ultimate bubble visual translation offset X")
    public float bubbleOffsetX = 0.0f;

    @Comment("Ultimate bubble visual translation offset Y")
    public float bubbleOffsetY = -0.5f;

    @Comment("Ultimate bubble visual translation offset Z")
    public float bubbleOffsetZ = 0.0f;

    // Messages
    @Comment("Message sent when shield passive protection activates")
    public String passiveActivatedMessage = "<gold><bold>SHIELD DISABLED!</bold> Gained <reduction>% damage reduction for <seconds> seconds!</gold>";

    @Comment("Message sent when shield ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Shield Ultimate is fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> blocks</gray>";

    @Comment("Message sent when shield ultimate is activated")
    public String ultimateActivatedMessage = "<green><bold>SHIELD ULTIMATE ACTIVATED!</bold> Gained God Mode bubble barrier!</green>";

    @Comment("Message sent when shield ultimate expires")
    public String ultimateExpiredMessage = "<red>Your Shield Ultimate bubble has expired!</red>";

    @Comment("Message sent when shield ultimate is on cooldown")
    public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";

    @Comment("Message sent when player is not holding a Shield for ultimate")
    public String mustHoldShieldMessage = "<red>You must be holding a Shield to activate your ultimate!</red>";

    @Comment("Message sent when player does not have enough strength for ultimate")
    public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

    @Comment("Message sent when player ultimate is not fully charged")
    public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> blocks)</red>";
}
