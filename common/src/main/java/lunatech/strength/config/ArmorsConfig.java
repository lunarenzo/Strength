package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Armors / Gear Set abilities (Passive Gear Auto-Upgrade & Ultimate Knockback Immunity + Golden Apple Absorption Boost).
 */
@ConfigSerializable
public class ArmorsConfig implements VersionedConfig {
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

    @Comment("Master toggle for Armors passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Armors Passive Ability Settings (Auto-Upgrade equipped base armor)")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Armors Ultimate Ability Settings (Full set Knockback Immunity + Golden Apple Boost)")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Armors passive gear auto-upgrade.")
        public boolean enabled = true;

        @Comment("Mapping of base armor material to upgraded target armor material.")
        public Map<String, String> upgrades = Map.of(
            "IRON_HELMET", "DIAMOND_HELMET",
            "IRON_CHESTPLATE", "DIAMOND_CHESTPLATE",
            "IRON_LEGGINGS", "DIAMOND_LEGGINGS",
            "IRON_BOOTS", "DIAMOND_BOOTS"
        );

        @Comment("Message sent when an armor piece is auto-upgraded.")
        public String armorUpgradedMessage = "<gold><bold>ARMOR PASSIVE!</bold> Your equipped <old_armor> was upgraded to <new_armor>!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Armors ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Armors Ultimate.")
        public int strengthRequired = 5;

        @Comment("Ultimate active duration in seconds.")
        public int durationSeconds = 20;

        @Comment("Cooldown in seconds for Armors Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("Absorption amplifier given when consuming a Golden Apple during Ultimate (e.g. 3 = Absorption IV).")
        public int goldenAppleAbsorptionAmplifier = 3;

        @Comment("Duration in seconds for the upgraded Golden Apple Absorption effect.")
        public int goldenAppleAbsorptionDurationSeconds = 120;

        @Comment("Message sent when Armors ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>ARMORS ULTIMATE ACTIVATED!</bold> Granted knockback immunity & Golden Apple absorption boost for <duration>s!</gold>";

        @Comment("Message sent when Armors ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Armors Ultimate has expired.</red>";

        @Comment("Message sent when player is not equipping a full valid armor set.")
        public String mustEquipFullSetMessage = "<red>You must be equipping a full set of armor to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";
    }
}
