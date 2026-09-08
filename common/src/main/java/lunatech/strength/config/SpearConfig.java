package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

/**
 * Decoupled configuration for Spear weapon abilities (Passive Faster Sword Attack Speed & Bonus Poke Damage, Ultimate Auto-Enchant & Zero Hunger Lunge).
 */
@ConfigSerializable
public class SpearConfig implements VersionedConfig {
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

    @Comment("Master toggle for Spear weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Spear Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Spear Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Spear passive abilities.")
        public boolean enabled = true;

        @Comment("Boost Spear attack cooldown speed to match Sword attack speed (1.6 speed / fast attack recovery).")
        public boolean matchSwordAttackSpeed = true;

        @Comment("Flat bonus damage added to every spear strike on an enemy.")
        public double bonusPokeDamage = 2.0;

        @Comment("Message sent on actionbar when Spear passive bonus damage applies.")
        public String passiveTriggeredMessage = "<gold><bold>SPEAR POKE!</bold> +<damage> bonus damage!</gold>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Spear ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Spear Ultimate.")
        public int strengthRequired = 5;

        @Comment("Number of spear hits required to charge the ultimate.")
        public int hitsRequired = 5;

        @Comment("Ultimate active duration in seconds.")
        public int durationSeconds = 15;

        @Comment("Cooldown in seconds for Spear Ultimate.")
        public int cooldownSeconds = 60;

        @Comment("Prevent hunger consumption when lunging with a spear during ultimate.")
        public boolean noHungerOnLunge = true;

        @Comment("List of enchantments automatically applied to Spear during ultimate (Format: ENCHANTMENT_KEY:LEVEL).")
        public List<String> autoEnchantments = List.of("LUNGE:3", "SHARPENED:4");

        @Comment("Message sent when Spear ultimate ability is disabled.")
        public String ultimateDisabledMessage = "<red>Spear ultimate ability is currently disabled!</red>";

        @Comment("Message sent when Spear ultimate is fully charged.")
        public String ultimateChargedMessage = "<gold><bold>SPEAR ULTIMATE READY!</bold> Type /ability to activate!</gold>";

        @Comment("Message sent indicating ultimate hit charge progress.")
        public String ultimateChargeProgressMessage = "<gray>Spear Hit Charge: <gold><current>/<req></gold> hits</gray>";

        @Comment("Message sent when Spear ultimate is activated.")
        public String ultimateActivatedMessage = "<gold><bold>SPEAR ULTIMATE ACTIVATED!</bold> Auto-enchanted & zero hunger lunge for <duration>s!</gold>";

        @Comment("Message sent when Spear ultimate expires.")
        public String ultimateExpiredMessage = "<red>Your Spear Ultimate has expired.</red>";

        @Comment("Message sent when player is not holding a Spear.")
        public String mustHoldSpearMessage = "<red>You must be holding a Spear to activate your ultimate!</red>";

        @Comment("Message sent when player does not have enough strength.")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is not fully charged.")
        public String notChargedMessage = "<red>Your Ultimate is not fully charged! (Required: <req> hits, Current: <current>)</red>";

        @Comment("Message sent when ultimate is on cooldown.")
        public String ultimateCooldownMessage = "<red>Your Ultimate is on cooldown for another <seconds>s!</red>";
    }
}
