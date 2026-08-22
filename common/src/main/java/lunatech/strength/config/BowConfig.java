package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class BowConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Hits required using bow to activate Passive (llama spit trail + cobweb)")
    public int passiveHitsRequired = 2;

    @Comment("Particle type used for Bow passive web arrow trail (e.g. ITEM, CLOUD, POOF)")
    public String passiveTrailParticleType = "ITEM";

    @Comment("Material used for Bow passive web arrow trail crumbs (e.g. COBWEB, WHITE_WOOL)")
    public String passiveTrailParticleMaterial = "COBWEB";

    @Comment("Duration in seconds that the faked cobweb traps the player")
    public int passiveCobwebDurationSeconds = 5;

    @Comment("Strength required to activate Ultimate")
    public int ultimateStrengthRequired = 5;

    @Comment("Hits required using bow to charge Ultimate")
    public int ultimateHitsRequired = 10;

    @Comment("Cooldown in seconds for the Bow Ultimate")
    public int ultimateCooldownSeconds = 60;

    @Comment("The material of the ultimate beam item display")
    public String beamMaterial = "NAUTILUS_SHELL";

    @Comment("The custom model data of the ultimate beam item display")
    public int beamCustomModelData = 12347;

    @Comment("The custom model data of the ultimate spiral item display")
    public int beamSpiralCustomModelData = 12347;

    @Comment("The range of the ultimate beam")
    public double ultimateRange = 20.0;

    @Comment("The width/radius of the ultimate beam")
    public double ultimateWidth = 1.5;

    @Comment("Damage dealt by the ultimate beam (in hearts / half-hearts)")
    public double ultimateDamage = 8.0;

    @Comment("Number of beams shot per ultimate activation")
    public int ultimateBeams = 3;

    // Messages
    @Comment("Message sent to shooter when bow passive is triggered")
    public String passiveTriggeredShooterMessage = "<gold>Fired a Llama Spit Web Arrow!</gold>";

    @Comment("Message sent to shooter when bow passive becomes ready")
    public String passiveReadyShooterMessage = "<gold><bold>Bow Passive is ready! Your next shot will trap the target in a cobweb!</bold></gold>";

    @Comment("Message sent to victim when trapped in a cobweb")
    public String passiveTrappedVictimMessage = "<red><bold>TRAPPED! You are caught in a cobweb!</bold></red>";

    @Comment("Message sent when bow ultimate is fully charged")
    public String ultimateChargedMessage = "<green><bold>Bow Ultimate is fully charged! Use /ability to activate!</bold></green>";

    @Comment("Message sent indicating ultimate charge progress")
    public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold></gray>";

    @Comment("Message sent when bow ultimate is activated")
    public String ultimateActivatedMessage = "<gold><bold>BOW ULTIMATE ACTIVATED!</bold> Preparing Sonic Blast Beams...</gold>";

    @Comment("Message sent when bow ultimate is on cooldown")
    public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";

    @Comment("Message sent when player is not holding a Bow for ultimate")
    public String mustHoldBowMessage = "<red>You must be holding a Bow to activate your ultimate!</red>";

    @Comment("Message sent when player does not have enough strength for ultimate")
    public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

    @Comment("Message sent when player ultimate is not fully charged")
    public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>";
}
