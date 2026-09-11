package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Axe weapon abilities (Passive Seismic Stun & Ultimate Executioner's Mark).
 */
@ConfigSerializable
public class AxeConfig implements VersionedConfig {
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

    @Comment("Master toggle for Axe weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Axe Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Axe Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Axe passive ability.")
        public boolean enabled = true;

        @Comment("Critical hits required using an Axe to trigger Seismic Stun passive")
        public int critsRequired = 5;

        @Comment("Duration in seconds of the stun effect")
        public int stunDurationSeconds = 1;

        @Comment("Whether stunned players are prevented from attacking during the stun duration")
        public boolean cancelAttacksWhenStunned = true;

        @Comment("Edge Case 1: Whether hits against a blocking shield count toward the critical hit counter")
        public boolean countShieldHitsAsCrit = false;

        @Comment("Message sent when Axe passive stun triggers")
        public String passiveTriggeredAttackerMessage = "<color:#ff0000>  <white>Axe Passive triggered!</color>";

        @Comment("Actionbar displayed on stunned target player")
        public String stunActionbarMessage = "";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Axe ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Axe Ultimate")
        public int strengthRequired = 5;

        @Comment("Critical hits required using an Axe to charge Ultimate")
        public int critsRequired = 0;

        @Comment("Duration of Executioner's Mark ultimate in seconds")
        public int durationSeconds = 10;

        @Comment("Multiplier applied to total stored damage when ultimate expires (e.g. 1.5x)")
        public double damageMultiplier = 1.5;

        @Comment("Cooldown in seconds before Axe Ultimate can be used again")
        public int cooldownSeconds = 60;

        // Executioner's Mark Visual FX Settings
        @Comment("Base64 texture string for floating skull ItemDisplay on marked player head")
        public String skullBase64Texture = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjUzMzQ0ODZiNzA4OGIwZDY2M2FjZjhlNzMxOWRmOWY2NmFmN2Y3OWI5YzkwZTI1YzVjM2MwMmYwNzc1Yjc4YiJ9fX0=";

        @Comment("Height offset in blocks for floating skull above marked player head")
        public double skullHeightOffset = 0.6;

        @Comment("Scale of the floating skull ItemDisplay")
        public double skullScale = 0.85;

        @Comment("Rotation speed in degrees per tick for floating skull (positive = clockwise, negative = counterclockwise)")
        public double skullRotationSpeedDegrees = 8.0;

        @Comment("Maximum view distance in blocks for marked player visual skull & particle effects")
        public double skullViewDistanceBlocks = 40.0;

        @Comment("Enable bleeding particle effect dripping from marked player body")
        public boolean enableBleedParticles = true;

        @Comment("Number of blood dripping particles spawned per burst")
        public int bleedParticleCount = 6;

        @Comment("Interval in ticks between blood dripping particle bursts")
        public int bleedParticleFrequencyTicks = 2;

        @Comment("Particle type used for bleeding effect (e.g. ITEM, BLOCK, DAMAGE_INDICATOR)")
        public String bleedParticleType = "ITEM";

        @Comment("Material used for bleeding particle crumbs (e.g. REDSTONE_BLOCK, NETHER_WART_BLOCK, RED_WOOL)")
        public String bleedParticleMaterial = "REDSTONE_BLOCK";

        @Comment("Velocity speed of blood dripping particles")
        public double bleedParticleSpeed = 0.08;

        // Messages
        @Comment("Message sent when Axe ultimate is disabled")
        public String ultimateDisabledMessage = "<red>Axe ultimate ability is currently disabled!</red>";

        @Comment("Message sent when Axe ultimate is fully charged")
        public String ultimateChargedMessage = "<green><bold>Axe Ultimate fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent when Axe ultimate is activated")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Axe Ultimate Activated!</color>";

        @Comment("Message sent when Axe ultimate expires and triggers burst damage")
        public String ultimateExpiredMessage = "<color:#ff0000>  <white>Axe Ultimate triggered!</color>";

        @Comment("Actionbar displayed on target accumulating pending damage")
        public String pendingDamageActionbarMessage = "";

        @Comment("Message sent when Axe Ultimate is on cooldown")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent when player is not holding an Axe")
        public String mustHoldAxeMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding an Axe to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: {req}, Current: {current})</red>";

        @Comment("Message sent when ultimate is not charged yet")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: {req}, Current: {current} critical hits)</red>";

        @Comment("Message sent when Axe ultimate is already active")
        public String alreadyActiveMessage = "<color:#ff0000>  <white>Ultimate already activated</color>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
