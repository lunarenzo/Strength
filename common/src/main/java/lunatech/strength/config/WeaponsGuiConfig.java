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
                "<gray>Every <gold>3 hits</gold> summons lightning dealing <red>2.0x</red> damage + <red>3.0</red> bonus damage.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Thunderous Barrage</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>8 hits</gold> | Cooldown: <green>16s</green></gray>",
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
                "<gray>Every <gold>3 hits</gold>, next arrow traps target in cobwebs for <gold>3s</gold> (10s CD).</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Face Spiral Ring (Beam)</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>8 hits</gold> | Cooldown: <green>15s</green></gray>",
                "<gray>Full bow draw (18+ ticks) fires a 30-block piercing laser beam (4.0 dmg + Slowness/Blindness).</gray>",
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
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Disable Safeguard</white>",
                "<gray>Disables/breaks grant temporary damage reduction safeguard.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Aegis Barrier (God Mode)</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>8 blocks</gold> | Cooldown: <green>30s</green></gray>",
                "<gray>Sneak + Right Click shield grants 100% invulnerability for <gold>5s</gold>.</gray>",
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
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Charged Shot Boost</white>",
                "<gray>Every <gold>3 hits</gold>, next crossbow shot deals bonus damage.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Tranquilizer Shot</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>8 hits</gold> | Cooldown: <green>20s</green></gray>",
                "<gray>Sneak + Right Click primes shot that freezes target position & rotation for <gold>3s</gold>.</gray>",
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
                "<yellow><bold>Passive Ability:</bold></yellow> <white>Auto-Crit Combo</white>",
                "<gray>Every <gold>3 hits</gold> within 3s triggers a guaranteed <red>1.5x Auto-Crit</red> strike.</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Dual Wielding (Berserker Stance)</gold>",
                "<gray>Activation: <yellow>/ability</yellow> | Req: Strength <red>5</red> | Charges: <gold>5 hits</gold> | CD: <green>18s</green></gray>",
                "<gray>Clones sword into off-hand for <gold>10s</gold>, granting +100% attack speed & dual-hand strikes.</gray>",
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
                "<gray>Every <gold>3 critical hits</gold> stuns target for <gold>2s</gold> (Slowness III + Weakness I).</gray>",
                "",
                "<yellow><bold>Ultimate Ability:</bold></yellow> <gold>Executioner's Mark</gold>",
                "<gray>Req: Strength <red>5</red> | Charges: <gold>5 hits</gold> | Cooldown: <green>25s</green></gray>",
                "<gray>Marks target for <gold>5s</gold>, storing 100% damage & detonating for <red>150% burst</red>.</gray>",
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
