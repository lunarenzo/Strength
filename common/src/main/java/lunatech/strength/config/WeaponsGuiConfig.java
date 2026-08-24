package lunatech.strength.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration model for the Weapons Chest GUI (/strength weapons).
 */
@ConfigSerializable
public class WeaponsGuiConfig implements VersionedConfig {
    @Comment("Do not change this value!")
    public int configVersion = 1;

    @Override
    public int configVersion() {
        return configVersion;
    }

    @Comment("Title of the Weapons Chest GUI (MiniMessage supported)")
    public String title = "<gradient:#FF5555:#FFAA00><bold>STRENGTH WEAPONS</bold></gradient>";

    @Comment("Rows of chest inventory (1 to 6)")
    public int rows = 3;

    @Comment("Filler background item settings")
    public GuiItemConfig filler = new GuiItemConfig(
        "GRAY_STAINED_GLASS_PANE",
        0,
        "<gray>",
        List.of()
    );

    @Comment("Player Profile Skull item settings (Row 1 slot by default)")
    public GuiSlotItemConfig playerSkull = new GuiSlotItemConfig(
        4,
        "PLAYER_HEAD",
        0,
        "<yellow><bold>{player}'s Profile</bold></yellow>",
        List.of(
            "<gray>Current Strength: <red><bold>{strength}</bold></red>",
            "<gray>Assigned Weapon: <gold><bold>{weapon}</bold></gold>",
            "",
            "<dark_gray>Use weapon abilities to fight!</dark_gray>"
        )
    );

    @Comment("Weapons display items in the GUI mapped by weapon key (trident, bow, shield, crossbow, sword, axe)")
    public Map<String, GuiSlotItemConfig> weapons = defaultWeaponsMap();

    private static Map<String, GuiSlotItemConfig> defaultWeaponsMap() {
        final Map<String, GuiSlotItemConfig> map = new HashMap<>();

        map.put("trident", new GuiSlotItemConfig(
            10,
            "TRIDENT",
            0,
            "<cyan><bold>⚡ TRIDENT OF POSEIDON</bold></cyan>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Lightning Strike</white>",
                "<gray>Every <gold>3</gold> hits summons lightning & deals <red>2x</red> damage + <red>3.0</red> bonus damage.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Thunderous Barrage</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>8</gold> hits | Cooldown: <green>16s</green></gray>",
                "<gray>Unleashes 9 thrust strikes with particle surges in a 4-block zone.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        map.put("bow", new GuiSlotItemConfig(
            11,
            "BOW",
            0,
            "<green><bold>🏹 BOW OF ARTEMIS</bold></green>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Cobweb Trap</white>",
                "<gray>Shots trap target in cobwebs for <gold>3s</gold> (10s CD).</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Face Spiral Ring</gold>",
                "<gray>Req: Strength <red>5</red> | Full Bow Draw (18 ticks) | Cooldown: <green>15s</green></gray>",
                "<gray>Fires a piercing spiral beam de-buffing enemies in its path.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        map.put("shield", new GuiSlotItemConfig(
            12,
            "SHIELD",
            0,
            "<blue><bold>🛡️ SHIELD OF ATHENA</bold></blue>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Shield Deflection</white>",
                "<gray>Blocking reflects <red>30%</red> incoming damage back to attacker.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Aegis Barrier</gold>",
                "<gray>Req: Strength <red>5</red> | Cooldown: <green>30s</green></gray>",
                "<gray>Grants invulnerability & knockback resistance for <gold>5s</gold>.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        map.put("crossbow", new GuiSlotItemConfig(
            13,
            "CROSSBOW",
            0,
            "<gold><bold>🏹 CROSSBOW OF APOLLO</bold></gold>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Immobilizing Bolt</white>",
                "<gray>Loaded bolts freeze targets for <gold>2s</gold>.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Volley Burst</gold>",
                "<gray>Req: Strength <red>5</red> | Cooldown: <green>20s</green></gray>",
                "<gray>Fires a 5-arrow explosive spread shot instantaneously.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        map.put("sword", new GuiSlotItemConfig(
            14,
            "DIAMOND_SWORD",
            0,
            "<red><bold>⚔️ SWORD OF ARES</bold></red>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Sweeping Blade</white>",
                "<gray>Sweeps deal <red>50%</red> collateral damage in a 3-block cone.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Blade Surge</gold>",
                "<gray>Req: Strength <red>5</red> | Cooldown: <green>18s</green></gray>",
                "<gray>Dashes forward dealing massive area slash damage.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        map.put("axe", new GuiSlotItemConfig(
            15,
            "NETHERITE_AXE",
            0,
            "<dark_red><bold>🪓 AXE OF HEPHAESTUS</bold></dark_red>",
            List.of(
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Seismic Stun</white>",
                "<gray>Slam attacks stun & slow nearby targets for <gold>2s</gold>.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Executioner's Mark</gold>",
                "<gray>Req: Strength <red>5</red> | Cooldown: <green>25s</green></gray>",
                "<gray>Marks enemy for 5s, storing damage & detonating for 150% total burst.</gray>",
                "",
                "<dark_gray>Status: {status}</dark_gray>"
            )
        ));

        return map;
    }

    @ConfigSerializable
    public static class GuiItemConfig {
        public String material = "GRAY_STAINED_GLASS_PANE";
        public int customModelData = 0;
        public String displayName = "<gray>";
        public List<String> lore = new ArrayList<>();

        public GuiItemConfig() {}

        public GuiItemConfig(String material, int customModelData, String displayName, List<String> lore) {
            this.material = material;
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.lore = lore;
        }
    }

    @ConfigSerializable
    public static class GuiSlotItemConfig {
        public int slot = 0;
        public String material = "STONE";
        public int customModelData = 0;
        public String displayName = "";
        public List<String> lore = new ArrayList<>();

        public GuiSlotItemConfig() {}

        public GuiSlotItemConfig(int slot, String material, int customModelData, String displayName, List<String> lore) {
            this.slot = slot;
            this.material = material;
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.lore = lore;
        }
    }
}
