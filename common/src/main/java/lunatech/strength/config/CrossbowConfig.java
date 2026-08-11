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

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Crossbow passive 3rd-shot triggers required to charge Ultimate")
    public int ultimateHitsRequired = 3;

    @Comment("Duration of the trap lockdown in seconds")
    public int trapDurationSeconds = 10;

    @Comment("Trigger radius to spring the trap (in blocks)")
    public double trapTriggerRadius = 3.0;

    @Comment("Maximum distance a trapped player can get from the anchor block before being pulled back (in blocks)")
    public double trapMaxDistance = 5.0;

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
    public String ultimateActivatedMessage = "<gold><bold>CROSSBOW ULTIMATE PRIMED!</bold> Your next arrow shot on a ground block will plant a trap anchor.</gold>";

    @Comment("Message sent when crossbow ultimate fails because the arrow hit an entity/player directly")
    public String ultimateFlippedMessage = "<red>Your ultimate shot hit a player directly and fizzled!</red>";

    @Comment("Message sent to the trapped player when they trigger the anchor")
    public String trapTriggeredVictimMessage = "<red><bold>TRAPPED! You have sprung a chain anchor trap!</bold></red>";

    @Comment("Message sent to the trapped player when they try to escape or teleport")
    public String trapEscapeBlockedMessage = "<red>You are tethered to the trap anchor and cannot escape!</red>";
}
