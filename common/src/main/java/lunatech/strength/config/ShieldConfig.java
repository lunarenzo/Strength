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

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Shield blocks required to charge Ultimate")
    public int ultimateHitsRequired = 10;

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
    @Comment("Message sent when shield ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Shield Ultimate is fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold> blocks</gray>";

    @Comment("Message sent when shield ultimate is activated")
    public String ultimateActivatedMessage = "<green><bold>SHIELD ULTIMATE ACTIVATED!</bold> Gained God Mode bubble barrier!</green>";

    @Comment("Message sent when shield ultimate expires")
    public String ultimateExpiredMessage = "<red>Your Shield Ultimate bubble has expired!</red>";
}
