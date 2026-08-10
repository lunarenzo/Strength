package lunatech.strength.config;

import lunatech.strength.config.exception.ConfigValidationException;
import lunatech.strength.config.migration.Migration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.interfaces.meta.Exclude;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.List;
import java.util.Map;

@ConfigSerializable
public class PluginConfig implements VersionedConfig {
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

    @Comment("Update Checker Settings")
    public UpdateChecker updateChecker = new UpdateChecker();

    @ConfigSerializable
    public static class UpdateChecker {
        @Comment("Should the plugin check for plugin updates on startup?")
        public boolean enabled = true;

        @Comment("Send update notifications to the console?")
        public boolean console = true;

        @Comment("Send update notifications to opped players on join?")
        public boolean op = true;
    }

    @Comment("Language, specify the language file to use, for strength `en_US` which will load `/lang/en_US.json`")
    public String language = "en_US";

    @Comment("Strength SMP Settings")
    public StrengthSettings strength = new StrengthSettings();

    @ConfigSerializable
    public static class StrengthSettings {
        @Comment("Amount of strength awarded to the killer on player kill")
        public int killReward = 1;

        @Comment("Amount of strength lost on death (use 0 to disable loss)")
        public int deathLoss = 1;

        @Comment("Minimum strength value a player can have")
        public int minStrength = 0;

        @Comment("Maximum strength value a player can have")
        public int maxStrength = 100;

        @Comment("Default strength value for new players")
        public int defaultStrength = 0;

        @Comment("Physical item settings for withdrawn strength")
        public WithdrawItemSettings withdrawItem = new WithdrawItemSettings();
    }

    @ConfigSerializable
    public static class WithdrawItemSettings {
        @Comment("The material of the physical strength item")
        public String material = "NAUTILUS_SHELL";

        @Comment("The custom model data of the physical strength item")
        public int customModelData = 12345;

        @Comment("The display name of the strength item (MiniMessage format)")
        public String displayName = "<gold><bold>Strength Shard</bold></gold>";

        @Comment("The lore of the strength item (MiniMessage format, <amount> will be replaced)")
        public List<String> lore = List.of(
            "<gray>Value: <gold><amount> Strength</gold></gray>",
            "<gray>Right-click to consume.</gray>"
        );
    }

    @Comment("Weapon Rolling and Passive/Ultimate Ability settings")
    public WeaponSettings weapons = new WeaponSettings();

    @ConfigSerializable
    public static class WeaponSettings {
        @Comment("Delay in seconds before triggering the weapon roll for a first-time player")
        public int rollDelaySeconds = 5;

        @Comment("The list of weapons available for rolling")
        public List<String> availableWeapons = List.of("Trident", "Sword", "Axe", "Bow", "Shield");

        @Comment("Title message shown when rolling starts")
        public String rollStartTitle = "<yellow>Assigning Weapon...</yellow>";

        @Comment("Trident Settings")
        public TridentSettings trident = new TridentSettings();

        @Comment("Bow Settings")
        public BowSettings bow = new BowSettings();

        @Comment("Shield Settings")
        public ShieldSettings shield = new ShieldSettings();
    }

    @ConfigSerializable
    public static class TridentSettings {
        @Comment("Hits required to summon lightning (Passive)")
        public int passiveHitsRequired = 4;

        @Comment("Lightning damage dealt on hit (Passive)")
        public double passiveLightningDamage = 2.5;

        @Comment("Strength required to activate Ultimate")
        public int ultimateStrengthRequired = 5;

        @Comment("Hits required using trident to charge Ultimate")
        public int ultimateHitsRequired = 8;

        @Comment("Ultimate speed multiplier")
        public double ultimateSpeed = 0.5;

        @Comment("Ultimate duration in ticks (20 ticks = 1 second)")
        public int ultimateDurationTicks = 100;
    }

    @ConfigSerializable
    public static class BowSettings {
        @Comment("Hits required using bow to activate Passive (llama spit trail + cobweb)")
        public int passiveHitsRequired = 2;

        @Comment("Duration in seconds that the faked cobweb traps the player")
        public int passiveCobwebDurationSeconds = 5;

        @Comment("Strength required to activate Ultimate")
        public int ultimateStrengthRequired = 5;

        @Comment("Hits required using bow to charge Ultimate")
        public int ultimateHitsRequired = 10;

        @Comment("The material of the ultimate beam item display")
        public String beamMaterial = "NAUTILUS_SHELL";

        @Comment("The custom model data of the ultimate beam item display")
        public int beamCustomModelData = 12348;

        @Comment("The custom model data of the ultimate spiral item display")
        public int beamSpiralCustomModelData = 12349;

        @Comment("The range of the ultimate beam")
        public double ultimateRange = 20.0;

        @Comment("The width/radius of the ultimate beam")
        public double ultimateWidth = 1.5;

        @Comment("Damage dealt by the ultimate beam (in hearts / half-hearts)")
        public double ultimateDamage = 8.0;

        @Comment("Number of beams shot per ultimate activation")
        public int ultimateBeams = 3;
    }

    @ConfigSerializable
    public static class ShieldSettings {
        @Comment("Strength required to activate Ultimate")
        public int ultimateStrengthRequired = 5;

        @Comment("Shield blocks required to charge Ultimate")
        public int ultimateHitsRequired = 10;

        @Comment("The material of the ultimate bubble item display")
        public String bubbleMaterial = "NAUTILUS_SHELL";

        @Comment("The custom model data of the ultimate bubble item display")
        public int bubbleCustomModelData = 12346;

        @Comment("Ultimate duration in ticks (20 ticks = 1 second, default 15s = 300 ticks)")
        public int ultimateDurationTicks = 300;
    }
}
