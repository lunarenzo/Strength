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
    public int passiveHitsRequired = 3;

    @Comment("Damage multiplier applied on the Nth passive strike (e.g. 2.0 = 2x damage)")
    public double passiveDamageMultiplier = 2.0;

    @Comment("Extra lightning bonus damage dealt on hit (Passive)")
    public double passiveLightningDamage = 3.0;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Hits required using trident to charge Ultimate")
    public int ultimateHitsRequired = 8;

    @Comment("Cooldown in seconds for Poseidon's Calling Ultimate")
    public int ultimateCooldownSeconds = 16;

    @Comment("Duration in ticks for Poseidon's Calling storm slam (20 ticks = 1 second)")
    public int ultimateDurationTicks = 20;

    @Comment("Radius in blocks for Poseidon's Calling area lightning strikes")
    public double ultimateRadius = 20.0;

    @Comment("Damage dealt by each Poseidon lightning strike")
    public double ultimateDamage = 15.0;

    @Comment("Interval in ticks between lightning strikes during Poseidon's Calling (e.g. every 5 ticks)")
    public int lightningStrikeIntervalTicks = 5;

    @Comment("Slowness amplifier applied to struck enemies (e.g. 5 = Slowness VI)")
    public int slownessAmplifier = 5;

    @Comment("Slowness duration in ticks applied to struck enemies (40 ticks = 2 seconds)")
    public int slownessDurationTicks = 40;

    @Comment("Custom model data for yellow lightning display entity (if resourcepack is loaded)")
    public int yellowLightningCustomModelData = 12350;

    // Messages
    @Comment("Message sent when trident passive is triggered")
    public String passiveTriggeredMessage = "<yellow><bold>POSEIDON PASSIVE!</bold> Lightning struck for extra damage!</yellow>";

    @Comment("Message sent when trident ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Poseidon's Calling is fully charged! Type /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> hits</gray>";

    @Comment("Message sent when Poseidon's Calling ultimate is activated")
    public String ultimateActivatedMessage = "<gold><bold>POSEIDON'S CALLING ACTIVATED!</bold> Slammed trident and summoned a lightning storm!</gold>";

    @Comment("Message sent when player is not on the ground or in water when activating Poseidon's Calling")
    public String mustBeOnGroundMessage = "<red>You must be standing on the ground or in water to perform Poseidon's Calling!</red>";

    @Comment("Message sent when player is not holding a Trident")
    public String mustHoldTridentMessage = "<red>You must be holding a Trident to activate your ultimate!</red>";

    @Comment("Message sent when player does not have enough strength")
    public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

    @Comment("Message sent when ultimate is not charged yet")
    public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>";

    @Comment("Message sent when ultimate is on cooldown")
    public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";
}
