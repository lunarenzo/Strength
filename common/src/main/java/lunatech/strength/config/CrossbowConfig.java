package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class CrossbowConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Number of valid crossbow arrow hits required to trigger passive damage multiplier")
    public int passiveHitsRequired = 3;

    @Comment("Damage multiplier applied when passive is triggered")
    public double passiveDamageMultiplier = 2.0;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Crossbow passive 3rd-shot triggers required to charge Ultimate")
    public int ultimateHitsRequired = 3;

    @Comment("Duration of the tranquilizer immobilization in seconds")
    public int immobilizeDurationSeconds = 5;

    // Messages
    @Comment("Message sent when crossbow passive 3rd shot deals 2x damage")
    public String passiveTriggeredShooterMessage = "<gold><bold>Crossbow Passive triggered! 2x damage dealt!</bold></gold>";

    @Comment("Message sent when slowness is applied to a fleeing target")
    public String slownessAppliedMessage = "<gray>Target was running away! Inflicted slowness.</gray>";

    @Comment("Message sent when crossbow ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Crossbow Ultimate is fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold> passive hits</gray>";

    @Comment("Message sent when crossbow ultimate is activated")
    public String ultimateActivatedMessage = "<gold><bold>CROSSBOW ULTIMATE ACTIVATED!</bold> Your next arrow shot on an enemy will immobilize them!</gold>";

    @Comment("Message sent to the shooter when ultimate hits an enemy")
    public String immobilizedShooterMessage = "<green><bold>CROSSBOW ULTIMATE HIT!</bold> Target immobilized!</green>";

    @Comment("Message sent to the victim when hit by ultimate shot")
    public String immobilizedVictimMessage = "<red><bold>IMMOBILIZED! You are struck by Crossbow Ultimate!</bold></red>";

    @Comment("Message sent to the immobilized player when attempting to move or teleport")
    public String trapEscapeBlockedMessage = "<red>You are immobilized and cannot move or teleport!</red>";
}
