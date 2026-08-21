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

    @Comment("Duration in ticks for Thunderous Barrage sequence (40 ticks = 2 seconds)")
    public int ultimateDurationTicks = 40;

    @Comment("Radius/cone length in blocks for Thunderous Barrage strike area")
    public double ultimateRadius = 4.0;

    @Comment("Damage dealt per thrust strike during Thunderous Barrage (9 strikes total)")
    public double ultimateDamage = 5.0;

    @Comment("Interval in ticks between thrust strikes during Thunderous Barrage (e.g. every 4 ticks)")
    public int lightningStrikeIntervalTicks = 4;

    @Comment("Slowness amplifier applied to caster during barrage (e.g. 1 = Slowness II)")
    public int slownessAmplifier = 1;

    @Comment("Slowness duration in ticks applied to caster during barrage (30 ticks = 1.5 seconds)")
    public int slownessDurationTicks = 30;

    @Comment("FreeMinecraftModels model ID for Thunderous Barrage visual effect")
    public String barrageModelId = "thunderous_barrage";

    @Comment("FreeMinecraftModels model ID for hit impact VFX")
    public String impactModelId = "vfx_hit_impact_1";

    @Comment("Yaw rotation offset in degrees for 3D model alignment relative to player direction (default -90.0)")
    public double modelYawOffsetDegrees = -90.0;

    // Messages
    @Comment("Message sent when trident passive is triggered")
    public String passiveTriggeredMessage = "<yellow><bold>POSEIDON PASSIVE!</bold> Lightning struck for extra damage!</yellow>";

    @Comment("Message sent when trident ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Thunderous Barrage is fully charged! Type /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> hits</gray>";

    @Comment("Message sent when Thunderous Barrage ultimate is activated")
    public String ultimateActivatedMessage = "<gold><bold>THUNDEROUS BARRAGE ACTIVATED!</bold> Unleashed a lightning fast strike barrage!</gold>";

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
