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

        @Comment("Virtual workbench GUI title showing the crafting recipe for Strength item (MiniMessage format)")
        public String guiTitle = "<dark_purple><bold>Recipe: Strength Shard</dark_purple>";

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

    @Comment("Physical item settings for weapon reroll item")
    public RerollItemSettings rerollItem = new RerollItemSettings();

    @ConfigSerializable
    public static class RerollItemSettings {
        @Comment("The material of the physical weapon reroll item")
        public String material = "BOOK";

        @Comment("The custom model data of the physical weapon reroll item")
        public int customModelData = 12346;

        @Comment("The display name of the reroll item (MiniMessage format)")
        public String displayName = "<light_purple><bold>Weapon Reroll Book</bold></light_purple>";

        @Comment("The lore of the reroll item (MiniMessage format)")
        public List<String> lore = List.of(
            "<gray>Right-click to reroll your assigned weapon.</gray>"
        );
    }

    @Comment("Craftable Weapon Reroll Book Recipe settings")
    public RerollRecipeSettings rerollRecipe = new RerollRecipeSettings();

    @ConfigSerializable
    public static class RerollRecipeSettings {
        @Comment("Should crafting the weapon reroll item be enabled?")
        public boolean enabled = true;

        @Comment("Virtual workbench GUI title showing the crafting recipe for Weapon Reroll Book (MiniMessage format)")
        public String guiTitle = "<light_purple><bold>Recipe: Weapon Reroll Book</light_purple>";

        @Comment("Crafting grid shape (3 lines of 3 characters each)")
        public List<String> shape = List.of(
            "SSS",
            "SBS",
            "SSS"
        );

        @Comment("Ingredients mapping for recipe shape characters. Supported formats:\n- STRENGTH_ITEM / STRENGTH_SHARD (Custom Strength Shard with CustomModelData 12345 & PDC)\n- cmd:12345:MATERIAL (CustomModelData format)\n- itemsadder:namespace:item_id\n- nexo:item_id\n- oraxen:item_id\n- Standard Bukkit Material (e.g. BOOK, DIAMOND_BLOCK)")
        public Map<String, String> ingredients = Map.of(
            "S", "STRENGTH_ITEM",
            "B", "BOOK"
        );
    }

    @Comment("Confirmation dialog / GUI settings when consuming a Weapon Reroll Book")
    public RerollDialogSettings rerollDialog = new RerollDialogSettings();

    @ConfigSerializable
    public static class RerollDialogSettings {
        @Comment("Should a confirmation dialog/GUI pop up when a player tries to consume a Weapon Reroll Book?")
        public boolean enabled = true;

        @Comment("Confirmation display mode: 'DIALOG' (Native Paper 1.21 Dialog API) or 'GUI' (TriumphGUI Chest Inventory)")
        public String mode = "DIALOG";

        @Comment("Title of the confirmation dialog/GUI (MiniMessage format)")
        public String title = "<light_purple><bold>Confirm Weapon Reroll</bold></light_purple>";

        @Comment("Message body inside the confirmation dialog/GUI (MiniMessage format)")
        public String message = "<gray>Are you sure you want to consume 1 Weapon Reroll Book to reroll your assigned weapon?</gray>";

        @Comment("Text for the Confirm / Yes button (MiniMessage format)")
        public String confirmButton = "<green><bold>YES</bold></green>";

        @Comment("Tooltip for the Confirm button (used in DIALOG mode)")
        public String confirmTooltip = "<gray>Click to confirm reroll</gray>";

        @Comment("Text for the Cancel / No button (MiniMessage format)")
        public String cancelButton = "<red><bold>NO</bold></red>";

        @Comment("Tooltip for the Cancel button (used in DIALOG mode)")
        public String cancelTooltip = "<gray>Click to cancel</gray>";

        @Comment("Chest GUI specific settings (used when mode is 'GUI' or as fallback)")
        public ChestGuiSettings gui = new ChestGuiSettings();
    }

    @ConfigSerializable
    public static class ChestGuiSettings {
        @Comment("Number of rows for the chest GUI (1 to 6)")
        public int rows = 3;

        @Comment("Slot for the Confirm (YES) button (0-indexed)")
        public int confirmSlot = 11;

        @Comment("Material for the Confirm button item")
        public String confirmMaterial = "LIME_CONCRETE";

        @Comment("Display name for the Confirm button item (MiniMessage format)")
        public String confirmDisplayName = "<green><bold>YES</bold></green>";

        @Comment("Lore lines for the Confirm button item (MiniMessage format)")
        public List<String> confirmLore = List.of("<gray>Click to confirm and consume reroll book.</gray>");

        @Comment("Slot for the Cancel (NO) button (0-indexed)")
        public int cancelSlot = 15;

        @Comment("Material for the Cancel button item")
        public String cancelMaterial = "RED_CONCRETE";

        @Comment("Display name for the Cancel button item (MiniMessage format)")
        public String cancelDisplayName = "<red><bold>NO</bold></red>";

        @Comment("Lore lines for the Cancel button item (MiniMessage format)")
        public List<String> cancelLore = List.of("<gray>Click to cancel.</gray>");

        @Comment("Slot for the Preview item (0-indexed)")
        public int previewSlot = 13;

        @Comment("Display name override for the Preview item (Leave blank to use dialog title)")
        public String previewDisplayName = "";

        @Comment("Lore lines override for the Preview item (Leave empty to use dialog message)")
        public List<String> previewLore = List.of();

        @Comment("Material for filler background items")
        public String fillerMaterial = "GRAY_STAINED_GLASS_PANE";

        @Comment("Display name for filler items")
        public String fillerDisplayName = "";
    }

    @Comment("AuthMe integration settings")
    public AuthMeSettings authme = new AuthMeSettings();

    @ConfigSerializable
    public static class AuthMeSettings {
        @Comment("Enable AuthMe integration (waits for player login/register before rolling weapon)")
        public boolean enabled = true;
    }

    @Comment("PvPManager integration settings")
    public PvPManagerSettings pvpmanager = new PvPManagerSettings();

    @ConfigSerializable
    public static class PvPManagerSettings {
        @Comment("Enable PvPManager integration")
        public boolean enabled = true;

        @Comment("Apply Strength loss penalty when a player combat logs out during PvP")
        public boolean handleCombatLogPenalty = true;

        @Comment("Prevent players from consuming reroll items or opening confirmation GUI while in combat")
        public boolean preventRerollInCombat = true;

        @Comment("Prevent players from withdrawing strength into physical items while in combat")
        public boolean preventWithdrawInCombat = true;
    }

    @Comment("WorldGuard integration settings")
    public WorldGuardSettings worldguard = new WorldGuardSettings();

    @ConfigSerializable
    public static class WorldGuardSettings {
        @Comment("Enable WorldGuard integration")
        public boolean enabled = true;

        @Comment("Disable weapon abilities in regions where PvP is DENY or strength-weapon-abilities is DENY")
        public boolean preventAbilitiesInSafezone = true;

        @Comment("Prevent strength loss on death in regions where strength-pvp-loss is DENY")
        public boolean preventStrengthLossInSafezone = true;

        @Comment("Prevent weapon rerolling in regions where strength-reroll is DENY")
        public boolean preventRerollInSafezone = true;
    }

    @Comment("Weapon Rolling and Passive/Ultimate Ability settings")
    public WeaponSettings weapons = new WeaponSettings();

    @ConfigSerializable
    public static class WeaponSettings {
        @Comment("Delay in seconds before triggering the weapon roll for a first-time player")
        public int rollDelaySeconds = 5;

        @Comment("The list of weapons available for rolling")
        public List<String> availableWeapons = List.of("Trident", "Sword", "Axe", "Bow", "Shield", "Crossbow");

        @Comment("Total number of animation steps during rolling")
        public int rollSteps = 15;

        @Comment("Initial tick delay between animation frames at the start of rolling (Fast)")
        public int initialStepDelayTicks = 1;

        @Comment("Final tick delay between animation frames at the end of rolling (Slow)")
        public int maxStepDelayTicks = 10;

        @Comment("Sound played when consuming a reroll book")
        public String consumeSound = "ITEM_BOOK_PAGE_TURN";

        @Comment("Volume for consume sound")
        public float consumeSoundVolume = 1.0f;

        @Comment("Pitch for consume sound")
        public float consumeSoundPitch = 1.0f;

        @Comment("Title message shown while rolling")
        public String rollStartTitle = "<yellow>Assigning Weapon...</yellow>";

        @Comment("Subtitle message shown while rolling (supports <weapon> placeholder)")
        public String rollSubtitle = "<gray>ROLLING: <gold><weapon></gold></gray>";

        @Comment("Main title message shown when a weapon is assigned (supports <weapon> placeholder)")
        public String assignedTitle = "<gold><bold><weapon></bold></gold>";

        @Comment("Subtitle message shown when a weapon is assigned (supports <weapon> placeholder)")
        public String assignedSubtitle = "<green>Weapon Assigned!</green>";

        @Comment("Custom display messages for specific weapons (overrides <weapon> placeholder if present)")
        public Map<String, String> weaponCustomMessages = Map.of(
            "Trident", "<aqua><bold>THUNDEROUS TRIDENT</bold></aqua>",
            "Sword", "<red><bold>INFERNAL SWORD</bold></red>",
            "Axe", "<dark_red><bold>EXECUTIONER AXE</bold></dark_red>",
            "Bow", "<green><bold>WIND BOW</bold></green>",
            "Shield", "<gold><bold>AEGIS SHIELD</bold></gold>",
            "Crossbow", "<purple><bold>VOID CROSSBOW</bold></purple>"
        );

        @Comment("Sound played during each animation step")
        public String rollTickSound = "UI_BUTTON_CLICK";

        @Comment("Volume for roll tick sound")
        public float rollTickVolume = 1.0f;

        @Comment("Pitch for roll tick sound")
        public float rollTickPitch = 1.2f;

        @Comment("Sound played when weapon assignment completes")
        public String completionSound = "ENTITY_PLAYER_LEVELUP";

        @Comment("Volume for completion sound")
        public float completionSoundVolume = 1.0f;

        @Comment("Pitch for completion sound")
        public float completionSoundPitch = 1.0f;

        @Comment("Delay in ticks before playing completion sound after title animation completes")
        public int completionSoundDelayTicks = 0;
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

        @Comment("Message sent when checking own strength (/strength or /strength info). Supports multiline with <newline> or <br>")
        public String strengthCheckMessage = "<light_purple>Your current Strength is <gold><strength></gold> and your assigned weapon is <gold><weapon></gold>.</light_purple>";

        @Comment("Fallback string for weapon placeholder when player has no weapon assigned")
        public String unassignedWeaponMessage = "None";

        @Comment("Message sent to sender when giving physical strength item(s)")
        public String giveStrengthItemSuccessSenderMessage = "<green>Gave <amount>x Strength Item(s) (Strength value: <value>) to <target>.</green>";

        @Comment("Message sent to target when receiving physical strength item(s)")
        public String giveStrengthItemSuccessTargetMessage = "<green>You received <amount>x Strength Item(s) (Strength value: <value>).</green>";

        @Comment("Message sent to sender when giving reroll item(s)")
        public String giveRollItemSuccessSenderMessage = "<green>Gave <amount>x Reroll Book(s) to <target>.</green>";

        @Comment("Message sent to target when receiving reroll item(s)")
        public String giveRollItemSuccessTargetMessage = "<green>You received <amount>x Reroll Book(s).</green>";

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

        @Comment("Message sent when consuming a Weapon Reroll Book")
        public String consumedRerollBookMessage = "<green>You consumed a Weapon Reroll Book!</green>";

        @Comment("Message sent when attempting to reroll weapons while in combat")
        public String cannotRerollInCombatMessage = "<red>You cannot reroll weapons while in combat!</red>";

        @Comment("Message sent when attempting to withdraw strength while in combat")
        public String cannotWithdrawInCombatMessage = "<red>You cannot withdraw strength while in combat!</red>";

        @Comment("Message sent when combat logging causes strength loss penalty")
        public String combatLogPenaltyMessage = "<red>You combat logged during PvP and lost <loss> Strength!</red>";

        @Comment("Message sent when attempting to use weapon ability in a WorldGuard protected region")
        public String cannotUseAbilityInRegionMessage = "<red>Weapon abilities are disabled in this region!</red>";

        @Comment("Message sent when attempting to reroll weapons in a WorldGuard protected region")
        public String cannotRerollInRegionMessage = "<red>Rerolling weapons is disabled in this region!</red>";
    }
}
