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

    // Messages
    @Comment("Message sent when sword passive Auto-Crit is triggered")
    public String passiveAutoCritMessage = "<gold><bold>Sword Passive triggered! AUTO-CRIT!</bold></gold>";

    @Comment("Message sent indicating combo progress")
    public String passiveComboProgressMessage = "<gray>Sword Combo: <yellow>{combo}/{required}</yellow></gray>";
}
