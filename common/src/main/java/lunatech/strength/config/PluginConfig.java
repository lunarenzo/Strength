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

        @Comment("Should the strength lost on death be dropped as a physical strength item?")
        public boolean dropItemOnDeath = false;

        @Comment("If dropItemOnDeath is enabled, should the killer ALSO receive direct strength added to their base? (Set false to prevent strength inflation)")
        public boolean giveDirectRewardWhenItemDropped = false;

        @Comment("Should killer only gain strength if the killed victim actually had strength to lose?")
        public boolean requireVictimStrengthForReward = true;

        @Comment("Should players lose strength (and drop strength items if enabled) on natural/non-PvP deaths (e.g. mobs, lava, fall)?")
        public boolean loseStrengthOnNaturalDeath = true;

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

    @Comment("Craftable Strength Item Recipe settings")
    public RecipeSettings recipe = new RecipeSettings();

    @ConfigSerializable
    public static class RecipeSettings {
        @Comment("Should crafting the physical strength item be enabled?")
        public boolean enabled = true;

        @Comment("Amount of strength stored in the crafted item")
        public int resultStrengthAmount = 1;

        @Comment("Crafting grid shape (3 lines of 3 characters each)")
        public List<String> shape = List.of(
            "DGD",
            "GNG",
            "DGD"
        );

        @Comment("Ingredients mapping for recipe shape characters")
        public Map<String, String> ingredients = Map.of(
            "D", "DIAMOND_BLOCK",
            "G", "GOLD_BLOCK",
            "N", "NETHER_STAR"
        );
    }

    @Comment("Weapon Rolling and Passive/Ultimate Ability settings")
    public WeaponSettings weapons = new WeaponSettings();

    @ConfigSerializable
    public static class WeaponSettings {
        @Comment("Delay in seconds before triggering the weapon roll for a first-time player")
        public int rollDelaySeconds = 5;

        @Comment("The list of weapons available for rolling")
        public List<String> availableWeapons = List.of("Trident", "Sword", "Axe", "Bow", "Shield", "Crossbow");

        @Comment("Title message shown when rolling starts")
        public String rollStartTitle = "<yellow>Assigning Weapon...</yellow>";

    }
}
