package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Bow weapon abilities (Passive Llama Spit Web Trap & Ultimate Laser Beam Shot).
 */
@ConfigSerializable
public class BowConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    @Exclude
    public int configVersion() {
        return configVersion;
    }

    @Override
    @Exclude
    public @NotNull Map<Integer, Migration> migrations() {
        return Map.of();
    }

    @Override
    @Exclude
    public void validate() throws ConfigValidationException {
    }

    @Comment("Master toggle for Bow weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Bow Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Bow Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Bow passive ability.")
        public boolean enabled = true;

        @Comment("Hits required using bow to activate Passive (llama spit trail + cobweb)")
        public int hitsRequired = 3;

        @Comment("Particle type used for Bow passive web arrow trail (e.g. ITEM, CLOUD, POOF)")
        public String trailParticleType = "ITEM";

        @Comment("Material used for Bow passive web arrow trail crumbs (e.g. COBWEB, WHITE_WOOL)")
        public String trailParticleMaterial = "COBWEB";

        @Comment("Duration in seconds that the faked cobweb traps the player")
        public int cobwebDurationSeconds = 5;

        @Comment("Message sent to shooter when bow passive is triggered")
        public String passiveTriggeredShooterMessage = "";

        @Comment("Message sent to shooter when bow passive becomes ready")
        public String passiveReadyShooterMessage = "<color:#ff0000>  <white>Bow Passive ready!</color>";

        @Comment("Message sent to victim when trapped in a cobweb")
        public String passiveTrappedVictimMessage = "";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Bow ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate")
        public int strengthRequired = 5;

        @Comment("Hits required using bow to charge Ultimate")
        public int hitsRequired = 0;

        @Comment("Cooldown in seconds for the Bow Ultimate")
        public int cooldownSeconds = 60;

        @Comment("The material of the ultimate beam item display")
        public String beamMaterial = "NAUTILUS_SHELL";

        @Comment("The custom model data of the ultimate beam item display")
        public int beamCustomModelData = 12348;

        @Comment("The custom model data of the ultimate spiral item display")
        public int beamSpiralCustomModelData = 12349;

        @Comment("The range of the ultimate beam")
        public double range = 20.0;

        @Comment("The width/radius of the ultimate beam")
        public double width = 1.5;

        @Comment("Damage dealt by the ultimate beam (in hearts / half-hearts)")
        public double damage = 40.0;

        @Comment("Multiplier applied per player strength point to scale ultimate damage (e.g. 0.1 for +10% damage per strength point, set to 0.0 to disable strength scaling)")
        public double strengthDamageMultiplier = 0.0;

        @Comment("Number of beams shot per ultimate activation")
        public int beams = 3;

        @Comment("Sound played during ultimate charge phase (vanilla sound enum, custom sound key like strength:beam.charge, or NONE)")
        public String chargeSound = "ENTITY_WARDEN_SONIC_CHARGE";

        @Comment("Secondary custom sound played alongside ultimateChargeSound during charge phase (e.g. strength:beam.charge, or NONE)")
        public String customChargeSound = "strength:beam.charge";

        @Comment("Sound played when ultimate beam fires (vanilla sound enum or custom sound key like strength:beam.fire, or NONE)")
        public String fireSound = "strength:beam.fire";

        @Comment("Message sent when bow ultimate is disabled")
        public String ultimateDisabledMessage = "<red>Bow ultimate ability is currently disabled!</red>";

        @Comment("Message sent when bow ultimate is fully charged")
        public String ultimateChargedMessage = "<green><bold>Bow Ultimate is fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold></gray>";

        @Comment("Message sent when bow ultimate is activated")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Bow Ultimate Activated! Next 3 shots will fire a beam</color>";

        @Comment("Message sent when bow ultimate is on cooldown")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent when player is not holding a Bow for ultimate")
        public String mustHoldBowMessage = "<red>You must be holding a Bow to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength for ultimate")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when player ultimate is not fully charged")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";

        @Comment("Message sent when firing a bow beam shot (supports <remaining>/<total> and {remaining}/{total} placeholders)")
        public String ultimateBeamFiredMessage = "<color:#ff0000>  <white><remaining>/<total> beams remaining";
    }
}
