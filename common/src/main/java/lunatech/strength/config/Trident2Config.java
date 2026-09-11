package lunatech.strength.config;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;

import java.util.Map;

/**
 * Decoupled configuration for the Trident2 weapon (Storm & Shield Stun Trident).
 */
@ConfigSerializable
public class Trident2Config implements VersionedConfig {
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

    @Comment("Master toggle for Trident2 weapon features & abilities.")
    public boolean enabled = true;

    @Comment("Trident2 Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Trident2 Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Trident2 passive abilities (sword attack speed & axe shield stun).")
        public boolean enabled = true;

        @Comment("Enable matching native Minecraft Sword attack speed (1.6 attacks/sec vs vanilla trident 1.1).")
        public boolean swordAttackSpeedEnabled = true;

        @Comment("Attack speed attribute bonus applied when holding Trident2 (0.5 adds to 1.1 base = 1.6 attack speed).")
        public double attackSpeedBonus = 0.5;

        @Comment("Enable axe-style shield stun on hitting blocking players with Trident2.")
        public boolean shieldStunEnabled = true;

        @Comment("Shield stun duration in seconds when breaking a blocking player's shield.")
        public double shieldStunDurationSeconds = 5.0;

        @Comment("Message sent to attacker when breaking target's shield.")
        public String shieldStunMessage = "<red><bold>SHIELD CRACK!</bold> You stunned <target>'s shield!</red>";

        @Comment("Message sent to victim when their shield is broken.")
        public String shieldStaggeredMessage = "<red><bold>STUNNED!</bold> Your shield was cracked by a Trident!</red>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Trident2 Thunderstorm ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Thunderstorm Ultimate.")
        public int strengthRequired = 5;

        @Comment("Cooldown in seconds for Thunderstorm Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("Duration in seconds of the active Thunderstorm state & on-crit lightning strikes.")
        public int durationSeconds = 15;

        @Comment("Extra bonus damage dealt per critical hit lightning strike during ultimate.")
        public double lightningBonusDamage = 4.0;

        @Comment("Particle type spawned on critical strike lightning hit (e.g. WAX_OFF).")
        public String lightningParticleType = "WAX_OFF";

        @Comment("Message sent when Thunderstorm Ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>THUNDERSTORM ULTIMATE ACTIVATED!</bold> Every crit summons lightning!</gold>";

        @Comment("Message sent when Thunderstorm Ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Thunderstorm Ultimate has expired!</red>";

        @Comment("Message sent when player is not holding a Trident.")
        public String mustHoldTridentMessage = "<red>You must be holding your Trident to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<gold><bold>⚡ ULTIMATE READY!</bold> Your <yellow>{weapon}</yellow> ultimate is ready to use! Type <yellow>/ability</yellow>!</gold>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "<gold><bold>⚡ {weapon} ULTIMATE READY!</bold></gold>";
    }
}
