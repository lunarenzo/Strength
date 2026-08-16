package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class AxeConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    // Passive Settings
    @Comment("Critical hits required using an Axe to trigger Seismic Stun passive")
    public int critsRequired = 4;

    @Comment("Duration in seconds of the stun effect")
    public int stunDurationSeconds = 3;

    @Comment("Whether stunned players are prevented from attacking during the stun duration")
    public boolean cancelAttacksWhenStunned = true;

    @Comment("Edge Case 1: Whether hits against a blocking shield count toward the critical hit counter")
    public boolean countShieldHitsAsCrit = false;

    // Ultimate Settings
    @Comment("Strength required to activate Axe Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Critical hits required using an Axe to charge Ultimate")
    public int ultimateCritsRequired = 5;

    @Comment("Duration of Executioner's Mark ultimate in seconds")
    public int ultimateDurationSeconds = 10;

    @Comment("Multiplier applied to total stored damage when ultimate expires (e.g. 1.5x)")
    public double damageMultiplier = 1.5;

    // Messages
    @Comment("Message sent when Axe passive stun triggers")
    public String passiveTriggeredAttackerMessage = "<gold><bold>Axe Passive Triggered!</bold> Target stunned for {seconds}s!</gold>";

    @Comment("Actionbar displayed on stunned target player")
    public String stunActionbarMessage = "<red><bold>⚡ STUNNED ({seconds}s)</bold></red>";

    @Comment("Message sent when Axe ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Axe Ultimate fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent when Axe ultimate is activated")
    public String ultimateActivatedMessage = "<dark_red><bold>AXE ULTIMATE ACTIVATED!</bold> All damage dealt for {seconds}s is stored and multiplied by {multiplier}x!</dark_red>";

    @Comment("Message sent when Axe ultimate expires and triggers burst damage")
    public String ultimateExpiredMessage = "<red><bold>AXE ULTIMATE FINISHED!</bold> Executioner's Mark triggered!</red>";

    @Comment("Actionbar displayed on target accumulating pending damage")
    public String pendingDamageActionbarMessage = "<dark_red><bold>☠ PENDING BURST DAMAGE: {amount}</bold></dark_red>";
}
