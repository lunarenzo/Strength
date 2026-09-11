package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Map;

/**
 * Decoupled configuration for Sword weapon abilities (Passive Auto-Crit Combo & Ultimate Dual Wielding).
 */
@ConfigSerializable
public class SwordConfig implements VersionedConfig {
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

    @Comment("Master toggle for Sword weapon passive and ultimate abilities.")
    public boolean enabled = true;

    @Comment("Sword Passive Ability Settings")
    public PassiveConfig passive = new PassiveConfig();

    @Comment("Sword Ultimate Ability Settings")
    public UltimateConfig ultimate = new UltimateConfig();

    @ConfigSerializable
    public static class PassiveConfig {
        @Comment("Enable or disable Sword passive ability.")
        public boolean enabled = true;

        @Comment("Consecutive sword hits required to trigger the Auto-Crit passive")
        public int comboHitsRequired = 4;

        @Comment("Maximum interval in seconds between hits before combo resets")
        public double comboTimeoutSeconds = 3.0;

        @Comment("Damage multiplier applied when Auto-Crit passive triggers")
        public double critDamageMultiplier = 1.5;

        @Comment("Message sent when sword passive Auto-Crit is triggered")
        public String passiveAutoCritMessage = "<color:#ff0000>  <white>Sword Passive triggered!</color>";

        @Comment("Message sent indicating combo progress")
        public String passiveComboProgressMessage = "<color:#ff0000>  <white>Sword Combo: {combo}/{required}</color>";
    }

    @ConfigSerializable
    public static class UltimateConfig {
        @Comment("Enable or disable Sword ultimate ability.")
        public boolean enabled = true;

        @Comment("Strength required to activate Ultimate")
        public int strengthRequired = 5;

        @Comment("Sword passive auto-crit triggers required to charge Ultimate")
        public int hitsRequired = 0;

        @Comment("Duration of Dual Wielding ultimate in seconds")
        public int durationSeconds = 10;

        @Comment("Maximum reach distance in blocks for offhand dual-wield attacks")
        public double offhandReachDistance = 3.5;

        @Comment("Base collateral sweep damage for offhand attacks")
        public double offhandSweepDamageMultiplier = 1.0;

        @Comment("Cooldown in seconds before Sword Ultimate can be used again")
        public int cooldownSeconds = 60;

        // Messages
        @Comment("Message sent when sword ultimate is disabled")
        public String ultimateDisabledMessage = "<red>Sword ultimate ability is currently disabled!</red>";

        @Comment("Message sent when sword ultimate is fully charged")
        public String ultimateChargedMessage = "<green><bold>Sword Ultimate is fully charged! Use /ability to activate!</bold></green>";

        @Comment("Message sent indicating ultimate charge progress")
        public String ultimateChargeProgressMessage = "<gray>Ultimate Charge: <gold>{charge}/{target}</gold> passive crits</gray>";

        @Comment("Message sent when sword ultimate is activated")
        public String ultimateActivatedMessage = "<color:#ff0000>  <white>Sword Ultimate Activated!</color>";

        @Comment("Message sent when sword ultimate expires")
        public String ultimateExpiredMessage = "<color:#ff0000>  <white>Sword Ultimate Deactivated!</color>";

        @Comment("Message sent when player is not holding a Sword")
        public String mustHoldSwordMessage = "<color:#ff0000> ᴇʀʀᴏʀ <gray>| <white>You must be holding a Sword to activate your ultimate!</color>";

        @Comment("Message sent when player does not have enough strength")
        public String notEnoughStrengthMessage = "<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>";

        @Comment("Message sent when ultimate is not charged yet")
        public String notChargedMessage = "<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> passive crits)</red>";

        @Comment("Actionbar progress bar message displayed while offhand attack is recharging during Dual Wielding")
        public String offhandChargingActionbarMessage = "<gray>Offhand: <gold><bar></gold></gray>";

        @Comment("Actionbar ready message displayed when offhand attack is fully charged during Dual Wielding")
        public String offhandReadyActionbarMessage = "";

        @Comment("Message sent when Sword Ultimate is on cooldown")
        public String ultimateCooldownMessage = "<color:#ff0000>  <white>Ultimate Cooldown Remaining: <seconds>s</color>";

        @Comment("Message sent in chat when ultimate cooldown completes and becomes ready")
        public String ultimateReadyMessage = "<color:#ff0000>  <white>ULTIMATE READY!</color>";

        @Comment("Message sent in actionbar when ultimate cooldown completes and becomes ready")
        public String ultimateReadyActionbarMessage = "";
    }
}
