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

    @Comment("""
        ================================================================================
         _____ _____     _   _ _____ _       ___ _____ _____ _____ _   _ _____ 
        |_   _/  ___|   | | | /  ___| |     / _ \\_   _|_   _/  ___| | | /  ___|
          | | \\ `--.    | |_| \\ `--.| |    / /_\\ \\| |   | | \\ `--.| |_| \\ `--. 
          | |  `--. \\   |  _  |`--. \\ |___ |  _  || |   | |  `--. \\  _  |`--. \\
          \\_/ \\____/    |_| |_|____/\\_____/|_| |_|\\_/   \\_/ \\____/|_| |_|____/ 
        
        Customize all plugin messages below. Set any message to "" (empty string) to disable sending it.
        ================================================================================
        """)
    public MessagesConfig messages = new MessagesConfig();

    @ConfigSerializable
    public static class MessagesConfig {
        @Comment("Message sent to victim when losing strength on death")
        public String deathLossMessage = "<red>You lost <loss> Strength on death. (New Strength: <strength>)</red>";

        @Comment("Message sent to killer when gaining strength for a kill")
        public String killRewardMessage = "<green>You gained +<reward> Strength for killing <victim>! (New Strength: <strength>)</green>";

        @Comment("Message sent to killer when victim has 0 strength to lose")
        public String killNoStrengthMessage = "<yellow><victim> had no Strength to lose, so no Strength was gained!</yellow>";

        @Comment("Message sent to killer when victim dropped a Strength Shard on death")
        public String killDroppedItemMessage = "<green>You killed <victim>! A Strength Shard has dropped on the ground!</green>";

        @Comment("Message sent when consuming a physical Strength Shard")
        public String consumeShardMessage = "<green>You consumed a Strength Shard and gained +<amount> Strength!</green>";

        @Comment("Message sent when checking own strength (/strength)")
        public String strengthCheckMessage = "<white>Your current strength level is: <gold><strength></gold>.</white>";

        @Comment("Message sent when failing to withdraw strength due to low balance")
        public String withdrawNotEnoughMessage = "<red>You do not have enough strength to withdraw <amount>! (Minimum required to keep: <min>, Current: <current>)</red>";

        @Comment("Message sent when failing to withdraw strength because inventory is full")
        public String withdrawFullInventoryMessage = "<red>Your inventory is full!</red>";

        @Comment("Message sent upon successfully withdrawing strength into a physical item")
        public String withdrawSuccessMessage = "<green>Successfully withdrew <amount> Strength into a physical item!</green>";

        @Comment("Message sent to sender when changing a player's assigned weapon")
        public String changeWeaponSuccessSenderMessage = "<green>Successfully changed <target>'s assigned weapon to <weapon>!</green>";

        @Comment("Message sent to target player when their assigned weapon is changed by admin")
        public String changeWeaponSuccessTargetMessage = "<gold>Your assigned weapon has been set to <weapon> by an admin!</gold>";

        @Comment("Message sent when providing an invalid weapon name")
        public String changeWeaponInvalidMessage = "<red>Invalid weapon type! Available: <list></red>";

        @Comment("Message sent upon setting a player's strength level via admin command")
        public String setStrengthSuccessMessage = "<green>Successfully set <target>'s strength from <old> to <amount>!</green>";

        @Comment("Message sent when target player is not found or offline")
        public String targetNotFoundMessage = "<red>Target player not found or offline!</red>";

        @Comment("Message sent when attempting to use weapon ability without an assigned weapon")
        public String noWeaponAssignedMessage = "<red>You have no weapon assigned! Weapon abilities are disabled.</red>";

        @Comment("Message sent when assigned weapon does not have an ultimate ability implemented")
        public String weaponNoUltimateMessage = "<red>Your assigned weapon (<weapon>) does not have an ultimate ability implemented.</red>";
    }
}
