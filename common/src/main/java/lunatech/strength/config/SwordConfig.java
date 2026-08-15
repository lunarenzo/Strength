package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class SwordConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Consecutive sword hits required to trigger the Auto-Crit passive")
    public int passiveComboHitsRequired = 4;

    @Comment("Maximum interval in seconds between hits before combo resets")
    public double passiveComboTimeoutSeconds = 2.5;

    @Comment("Damage multiplier applied when Auto-Crit passive triggers")
    public double passiveCritDamageMultiplier = 1.5;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Sword passive auto-crit triggers required to charge Ultimate")
    public int ultimateHitsRequired = 3;

    @Comment("Duration of Dual Wielding ultimate in seconds")
    public int ultimateDurationSeconds = 10;

    // Messages
    @Comment("Message sent when sword passive Auto-Crit is triggered")
    public String passiveAutoCritMessage = "<gold><bold>Sword Passive triggered! AUTO-CRIT!</bold></gold>";

    @Comment("Message sent indicating combo progress")
    public String passiveComboProgressMessage = "<gray>Sword Combo: <yellow>{combo}/{required}</yellow></gray>";

    @Comment("Message sent when sword ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Sword Ultimate is fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold> passive crits</gray>";

    @Comment("Message sent when sword ultimate is activated")
    public String ultimateActivatedMessage = "<gold><bold>SWORD ULTIMATE ACTIVATED!</bold> Dual Wielding enabled (+50% Cooldown Reduction)!</gold>";

    @Comment("Message sent when sword ultimate expires")
    public String ultimateExpiredMessage = "<red>Your Dual Wielding Sword Ultimate has expired!</red>";
}
