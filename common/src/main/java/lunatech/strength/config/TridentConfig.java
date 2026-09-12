package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Trident weapon abilities (Passive Lightning Strike & Multi-Thrust Thunderous Barrage Ultimate).
 */
@ConfigSerializable
public class TridentConfig implements VersionedConfig {
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

    @Comment("Master toggle for Trident weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Trident Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Trident Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Trident passive abilities.")
        public boolean enabled = true;

        @Comment("Hits required using trident to summon lightning.")
        public int hitsRequired = 3;

        @Comment("Damage multiplier applied on the Nth passive strike (e.g., 2.0 = 2x damage).")
        public double damageMultiplier = 1.0;

        @Comment("Extra lightning bonus damage dealt on hit (Passive).")
        public double lightningDamage = 3.0;

        @Comment("Particle type spawned on Trident passive hit (e.g. WAX_OFF, SCRAPE).")
        public String particleType = "WAX_OFF";

        @Comment("Message sent when trident passive is triggered.")
        public String passiveTriggeredMessage = "<color:#ff0000>  <white>Trident Passive triggered!</color>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Trident ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate.")
        public int strengthRequired = 5;

        @Comment("Hits required using trident to charge Ultimate.")
        public int hitsRequired = 0;

        @Comment("Cooldown in seconds for Poseidon's Calling Ultimate.")
        public int cooldownSeconds = 30;

        @Comment("Duration in ticks for Thunderous Barrage sequence (40 ticks = 2 seconds).")
        public int durationTicks = 40;

        @Comment("Forward reach length in blocks for Thunderous Barrage strike area.")
        public double radius = 4.0;

        @Comment("Maximum lateral width in blocks for Thunderous Barrage strike area (e.g. 2.0 = 1 block left, 1 block right).")
        public double widthBlocks = 2.0;

        @Comment("Damage dealt per thrust strike during Thunderous Barrage (9 strikes total).")
        public double damage = 5.0;

        @Comment("Scale damage dealt during Thunderous Barrage by adding player's current Strength level (damage + strength).")
        public boolean scaleDamageWithStrength = true;

        @Comment("Flat bonus damage added per player Strength point (e.g., 1.0 = +1.0 damage per Strength point).")
        public double strengthDamageBonusPerPoint = 0.0;

        @Comment("Interval in ticks between thrust strikes during Thunderous Barrage (e.g. every 4 ticks).")
        public int strikeIntervalTicks = 4;

        @Comment("Slowness amplifier applied to caster during barrage (e.g. 1 = Slowness II).")
        public int slownessAmplifier = 1;

        @Comment("Slowness duration in ticks applied to caster during barrage (30 ticks = 1.5 seconds).")
        public int slownessDurationTicks = 30;

        @Comment("FreeMinecraftModels model ID for Thunderous Barrage visual effect.")
        public String barrageModelId = "thunderous_barrage";

        @Comment("FreeMinecraftModels model ID for hit impact VFX.")
        public String impactModelId = "vfx_hit_impact_1";

        @Comment("Maximum number of thrust damage hits executed during the barrage (default 9).")
        public int maxBarrageHits = 9;

        @Comment("Yaw rotation offset in degrees for 3D model alignment relative to player direction (default 0.0).")
        public double modelYawOffsetDegrees = 0.0;

        @Comment("Message sent when trident ultimate ability is disabled.")
        public String ultimateDisabledMessage = "<red>Trident ultimate ability is currently disabled!</red>";

        @Comment("Message sent when trident ultimate is fully charged.")
        public String ultimateChargedMessage = "<green><bold>Thunderous Barrage is fully charged! Type /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress.")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold><charge>/<target></gold> hits</gray>";

        @Comment("Message sent when Thunderous Barrage ultimate is activated.")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Trident Ultimate Activated!</color>";

        @Comment("Message sent when player is not on the ground or in water when activating Poseidon's Calling.")
        public String mustBeOnGroundMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be on ground to use that ability!</color>";

        @Comment("Message sent when player is not holding a Trident.")
        public String mustHoldTridentMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding a Trident to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is not charged yet.")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
