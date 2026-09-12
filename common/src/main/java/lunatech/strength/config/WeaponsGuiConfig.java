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
    public String title = "<color:#ff0000><bold>ᴡᴇᴀᴘᴏɴ ᴄʟᴀѕѕ ɪɴꜰᴏ</bold></color>";

    @Comment("Rows of chest inventory (1 to 6)")
    public int rows = 5;

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
        "<color:#ff0000><bold>ʏᴏᴜʀ ᴘʀᴏꜰɪʟᴇ</bold></color>",
        List.of(
            "",
            "<color:#ff0000>ᴄᴜʀʀᴇɴᴛ ѕᴛʀᴇɴɢᴛʜ: <white>{strength}</white></color>",
            "<color:#ff0000>ᴀѕѕɪɢɴᴇᴅ ᴡᴇᴀᴘᴏɴ: <white>{weapon}</white></color>",
            ""
        )
    );

    @Comment("Strength Item display settings in the Weapons GUI")
    public GuiSlotItemConfig strengthItem = new GuiSlotItemConfig(
        12,
        "NAUTILUS_SHELL",
        12345,
        "<color:#ff0000><bold>ѕᴛʀᴇɴɢᴛʜ ѕʜᴀʀᴅ</bold></color>",
        List.of(
            "<white>ᴘʜʏѕɪᴄᴀʟ ѕʜᴀʀᴅ ᴄᴏɴᴛᴀɪɴɪɴɢ</white>",
            "<white>ᴘʟᴀʏᴇʀ ѕᴛʀᴇɴɢᴛʜ.</white>",
            "",
            "<white>ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄʀᴀꜰᴛɪɴɢ ʀᴇᴄɪᴘᴇ!</white>"
        ),
        "<white>"
    );

    @Comment("Reroll Book display item settings in the Weapons GUI")
    public GuiSlotItemConfig rerollItem = new GuiSlotItemConfig(
        14,
        "BOOK",
        12347,
        "<color:#ff0000><bold>ᴡᴇᴀᴘᴏɴ ᴄʟᴀѕѕ ʀᴇʀᴏʟʟ ʙᴏᴏᴋ</bold></color>",
        List.of(
            "<white>ᴜѕᴇᴅ ᴛᴏ ʀᴇʀᴏʟʟ ʏᴏᴜʀ ᴀѕѕɪɢɴᴇᴅ ᴡᴇᴀᴘᴏɴ</white>",
            "<white>ᴄᴏɴѕᴜᴍᴇ ɪɴ ʜᴀɴᴅ ᴛᴏ ᴏᴘᴇɴ ʀᴇʀᴏʟʟ ᴍᴇɴᴜ</white>",
            "",
            "<white>ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄʀᴀꜰᴛɪɴɢ ʀᴇᴄɪᴘᴇ!</white>"
        ),
        "<white>"
    );

    @Comment("Weapons display items in the GUI mapped by weapon key (trident, bow, shield, crossbow, sword, axe, spear, trident2, crossbow2, mace, armors)")
    public Map<String, GuiSlotItemConfig> weapons = defaultWeaponsMap();

    private static Map<String, GuiSlotItemConfig> defaultWeaponsMap() {
        final Map<String, GuiSlotItemConfig> map = new HashMap<>();

        map.put("shield", new GuiSlotItemConfig(
            40,
            "SHIELD",
            0,
            "<color:#ff0000>⛨ <bold>ѕʜɪᴇʟᴅ</bold></color>",
            List.of(
                "<color:#ff0000>⛨ <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕʜɪᴇʟᴅ ᴅɪѕᴀʙʟᴇ ɢʀᴀɴᴛѕ 20%</white></color>",
                "  ᴅᴀᴍᴀɢᴇ ʀᴇᴅᴜᴄᴛɪᴏɴ ꜰᴏʀ 5ѕ",
                "",
                "<color:#ff0000>⛨ <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴘᴀᴡɴѕ ᴀ ʙᴀʀʀɪᴇʀ</white></color>",
                "  ᴛʜᴀᴛ ɢʀᴀɴᴛѕ 100%",
                "  ɪɴᴠᴜʟɴᴇʀᴀʙɪʟɪᴛʏ ꜰᴏʀ 15ѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("sword", new GuiSlotItemConfig(
            20,
            "DIAMOND_SWORD",
            0,
            "<color:#ff0000> <bold>ѕᴡᴏʀᴅ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴅᴏ ᴀ 4-ʜɪᴛ ѕᴡᴏʀᴅ ᴄᴏᴍʙᴏ</white></color>",
                "  ᴡɪᴛʜɪɴ 3 ѕᴇᴄᴏɴᴅѕ ᴛᴏ",
                "  ᴅᴇᴀʟ 1.5x ᴄʀɪᴛ ᴅᴀᴍᴀɢᴇ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴄᴛɪᴠᴀᴛᴇѕ ᴅᴜᴀʟ ᴡɪᴇʟᴅ</white></color>",
                "  ɢʀᴀɴᴛѕ +100% ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ",
                "  ᴀɴᴅ ᴅᴜᴀʟ-ʜᴀɴᴅ ѕᴛʀɪᴋᴇѕ",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("spear", new GuiSlotItemConfig(
            29,
            "DIAMOND_SPEAR",
            0,
            "<color:#ff0000> <bold>ѕᴘᴇᴀʀ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ʙᴏᴏѕᴛѕ ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ ᴛᴏ ʙᴇᴄᴏᴍᴇ ʟɪᴋᴇ</white></color>",
                "  ᴀ ѕᴡᴏʀᴅ ᴀɴᴅ ᴅᴇᴀʟѕ +2.0 ʙᴏɴᴜѕ",
                "  ᴅᴀᴍᴀɢᴇ ᴏɴ ᴘᴏᴋᴇ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ѕᴘᴇᴀʀ with ʟᴜɴɢᴇ 3</white></color>",
                "  ᴀɴᴅ ʀᴇᴍᴏᴠᴇѕ ʜᴜɴɢᴇʀ ᴄᴏɴѕᴜᴍᴘᴛɪᴏɴ ᴅᴜʀɪɴɢ",
                "  ʟᴜɴɢᴇѕ ꜰᴏʀ 15ѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("trident", new GuiSlotItemConfig(
            22,
            "TRIDENT",
            0,
            "<color:#ff0000> <bold>ᴛʀɪᴅᴇɴᴛ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 3 ʜɪᴛѕ ѕᴜᴍᴍᴏɴѕ</white></color>",
                "  ʟɪɢʜᴛɴɪɴɢ, ᴅᴇᴀʟɪɴɢ 4.0 ᴀᴅᴅɪᴛɪᴏɴᴀʟ ᴅᴍɢ",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴜɴʟᴇᴀѕʜᴇѕ 9 ᴛʜʀᴜѕᴛ ѕᴛʀɪᴋᴇѕ ᴡɪᴛʜ</white></color>",
                "  ᴇᴀᴄʜ ѕᴛʀɪᴋᴇ ᴅᴇᴀʟɪɴɢ 5.0 ᴅᴍɢ",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 30 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("crossbow", new GuiSlotItemConfig(
            24,
            "CROSSBOW",
            0,
            "<color:#ff0000> <bold>ᴄʀᴏѕѕʙᴏᴡ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 3 ʜɪᴛѕ ᴅᴇᴀʟѕ 2.0x ᴅᴍɢ.</white></color>",
                "  ѕʜᴏᴏᴛɪɴɢ ꜰʟᴇᴇɪɴɢ ᴛᴀʀɢᴇᴛѕ",
                "  ɪɴꜰʟɪᴄᴛѕ ѕʟᴏᴡɴᴇѕѕ ꜰᴏʀ 3ѕ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴘʀɪᴍᴇѕ ɴᴇxᴛ ᴀʀʀᴏᴡ ᴛʜᴀᴛ</white></color>",
                "  ᴛᴀᴋᴇѕ ᴛʜᴇᴍ ᴅᴏᴡɴ ꜰᴏʀ 3ѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("bow", new GuiSlotItemConfig(
            23,
            "BOW",
            0,
            "<color:#ff0000>🏹 <bold>ʙᴏᴡ</bold></color>",
            List.of(
                "<color:#ff0000>🏹 <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 3 ʜɪᴛѕ, ɴᴇxᴛ ᴀʀʀᴏᴡ</white></color>",
                "  ᴛʀᴀᴘѕ ᴛᴀʀɢᴇᴛ ɪɴ ᴄᴏʙᴡᴇbs ғᴏʀ 5s",
                "",
                "<color:#ff0000>🏹 <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴘʀɪᴍᴇѕ 3 ʙᴇᴀᴍ ѕʜᴏᴛѕ</white></color>",
                "  (ꜰᴜʟʟ ᴅʀᴀws ꜰɪʀᴇѕ ᴀ 20-ʙʟᴏᴄᴋ",
                "  ʟᴀѕᴇʀ ᴅᴇᴀʟɪɴɢ 40.0 ᴅᴍɢ)",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("armors", new GuiSlotItemConfig(
            33,
            "DIAMOND_CHESTPLATE",
            0,
            "<color:#ff0000> <bold>ᴀʀᴍᴏʀ (ɢᴇᴀʀ ѕᴇᴛ)</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇǫᴜɪᴘᴘɪɴɢ ᴅɪᴀᴍᴏɴᴅ ᴀʀᴍᴏʀ</white></color>",
                "  ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ ᴜᴘɢʀᴀᴅᴇѕ ɪᴛ",
                "  ᴛᴏ ɴᴇᴛʜᴇʀɪᴛᴇ ᴀʀᴍᴏʀ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ɢʀᴀɴᴛѕ 100% ᴋɴᴏᴄᴋʙᴀᴄᴋ ɪᴍᴍᴜɴɪᴛʏ</white></color>",
                "  ᴀɴᴅ ᴀᴍᴘʟɪғʏ ɢᴏʟᴅᴇɴ ᴀᴘᴘʟᴇ",
                "  ᴀʙѕᴏʀᴘᴛɪᴏɴ ᴇғғᴇᴄᴛ ᴛᴏ 4 ꜰᴏʀ 20ѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("trident2", new GuiSlotItemConfig(
            30,
            "TRIDENT",
            0,
            "<color:#ff0000> <bold>S5 ᴛʀɪᴅᴇɴᴛ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴍᴀᴛᴄʜᴇѕ ѕᴡᴏʀᴅ ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ</white></color>",
                "  ᴀɴᴅ ѕᴛᴜɴѕ ʙʟᴏᴄᴋɪɴɢ ѕʜɪᴇʟᴅѕ",
                "  ꜰᴏʀ 5ѕ ᴏɴ ʜɪᴛ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴜᴍᴍᴏɴѕ ᴀ 15ѕ ᴛʜᴜɴᴅᴇʀѕᴛᴏʀᴍ</white></color>",
                "  ᴡʜᴇʀᴇ ᴇᴠᴇʀʏ ᴄʀɪᴛɪᴄᴀʟ ѕᴛʀɪᴋᴇ",
                "  ᴅᴇᴀʟѕ +4.0 ʙᴏɴᴜѕ ᴅᴍɢ ᴡɪᴛʜ",
                "  ʟɪɢʜᴛɴɪɴɢ ѕᴛʀɪᴋᴇѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("axe", new GuiSlotItemConfig(
            21,
            "DIAMOND_AXE",
            0,
            "<color:#ff0000> <bold>ᴀxᴇ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴅᴏ 5 ᴄʀɪᴛ ʜɪᴛѕ ᴛᴏ ᴀɴ ᴇɴᴇᴍʏ</white></color>",
                "  ᴛᴏ ѕᴛᴜɴ ᴛʜᴇᴍ ꜰᴏʀ 1 ѕᴇᴄᴏɴᴅ",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴍᴀʀᴋѕ ᴛᴀʀɢᴇᴛ ꜰᴏʀ 10ѕ</white></color>",
                "  ѕᴛᴏʀɪɴɢ 100% ᴅᴀᴍᴀɢᴇ ᴀɴᴅ",
                "  ᴅᴇᴛᴏɴᴀᴛɪɴɢ ɪᴛ ꜰᴏʀ 1.5x",
                "  ʙᴜʀѕᴛ ᴅᴀᴍᴀɢᴇ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("crossbow2", new GuiSlotItemConfig(
            31,
            "CROSSBOW",
            0,
            "<color:#ff0000> <bold>S5 ᴄʀᴏѕѕʙᴏᴡ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀʀʀᴏws ʙʏᴘᴀѕѕ ѕʜɪᴇʟᴅ ʙʟᴏᴄᴋѕ</white></color>",
                "  ᴀɴᴅ 35% ᴄʜᴀɴᴄᴇ ᴛᴏ ѕᴇᴛ ꜰɪʀᴇ",
                "  ꜰᴏʀ 5ѕ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ᴄʀᴏѕѕʙᴏᴡ ᴀɴᴅ</white></color>",
                "  ɢʀᴀɴᴛѕ ᴘᴏᴡᴇʀ 5 ᴀɴᴅ ǫᴜɪᴄᴋ",
                "  ᴄʜᴀʀɢᴇ 5 ꜰᴏʀ 10s.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("mace", new GuiSlotItemConfig(
            32,
            "MACE",
            0,
            "<color:#ff0000> <bold>ᴍᴀᴄᴇ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴍᴀѕʜ ᴀᴛᴛᴀᴄᴋ ᴄᴏᴏʟᴅᴏᴡɴ ɪѕ</white></color>",
                "  ʀᴇᴅᴜᴄᴇᴅ ʙʏ 50% ᴏɴ",
                "  ѕᴍᴀѕʜ ѕᴛʀɪᴋᴇѕ.",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ᴍᴀᴄᴇ with ᴡɪɴᴅ ʙᴜʀѕᴛ 2</white></color>",
                "  ᴀɴᴅ ɢʀᴀɴᴛѕ ᴢᴇʀᴏ ѕᴍᴀѕʜ ᴄᴏᴏʟᴅᴏᴡɴ",
                "  ꜰᴏʀ 30ѕ.",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
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
        public boolean enchanted = false;

        public GuiItemConfig() {}

        public GuiItemConfig(String material, int customModelData, String displayName, List<String> lore) {
            this(material, customModelData, displayName, lore, false);
        }

        public GuiItemConfig(String material, int customModelData, String displayName, List<String> lore, boolean enchanted) {
            this.material = material;
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.lore = lore;
            this.enchanted = enchanted;
        }
    }

    @ConfigSerializable
    public static class GuiSlotItemConfig {
        public int slot = 0;
        public String material = "STONE";
        public int customModelData = 0;
        public String displayName = "";
        public List<String> lore = new ArrayList<>();
        @Comment("Title of the virtual workbench GUI when clicked to preview recipe (MiniMessage format)")
        public String recipeGuiTitle = "";
        @Comment("Whether the item should have an enchantment glow effect in the GUI")
        public boolean enchanted = false;

        public GuiSlotItemConfig() {}

        public GuiSlotItemConfig(int slot, String material, int customModelData, String displayName, List<String> lore) {
            this(slot, material, customModelData, displayName, lore, "", false);
        }

        public GuiSlotItemConfig(int slot, String material, int customModelData, String displayName, List<String> lore, String recipeGuiTitle) {
            this(slot, material, customModelData, displayName, lore, recipeGuiTitle, false);
        }

        public GuiSlotItemConfig(int slot, String material, int customModelData, String displayName, List<String> lore, String recipeGuiTitle, boolean enchanted) {
            this.slot = slot;
            this.material = material;
            this.customModelData = customModelData;
            this.displayName = displayName;
            this.lore = lore;
            this.recipeGuiTitle = recipeGuiTitle;
            this.enchanted = enchanted;
        }
    }
}
