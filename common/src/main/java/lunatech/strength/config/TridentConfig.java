package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class TridentConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Hits required to summon lightning (Passive)")
    public int passiveHitsRequired = 4;

    @Comment("Lightning damage dealt on hit (Passive)")
    public double passiveLightningDamage = 2.5;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Hits required using trident to charge Ultimate")
    public int ultimateHitsRequired = 8;

    @Comment("Ultimate speed multiplier")
    public double ultimateSpeed = 0.5;

    @Comment("Ultimate duration in ticks (20 ticks = 1 second)")
    public int ultimateDurationTicks = 100;

    @Comment("Custom model data for yellow lightning bolt display entity")
    public int yellowLightningCustomModelData = 12350;

    // Messages
    @Comment("Message sent when trident passive is triggered")
    public String passiveTriggeredMessage = "<yellow><bold>Trident Passive triggered! Lightning struck!</bold></yellow>";

    @Comment("Message sent when trident ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Trident Ultimate is fully charged! Type /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold></gray>";

    @Comment("Message sent when riptide ultimate is activated")
    public String ultimateActivatedMessage = "<blue><bold>RIPTIDE WAVE ACTIVATED!</bold> Riding the waves...</blue>";

    @Comment("Message sent when ultimate ends due to colliding with a wall")
    public String ultimateCollisionWallMessage = "<red>Collided with a wall! Ability ended.</red>";

    @Comment("Message sent when ultimate ends due to colliding with a ceiling")
    public String ultimateCollisionCeilingMessage = "<red>Collided with a ceiling! Ability ended.</red>";
}
