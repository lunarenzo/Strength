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
            "<white>  ᴘʟᴀʏᴇʀ ѕᴛʀᴇɴɢᴛʜ.</white>",
            "",
            "<white>ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄʀᴀꜰᴛɪɴɢ</white>",
            "<white>  ʀᴇᴄɪᴘᴇ!</white>"
        ),
        ""
    );

    @Comment("Reroll Book display item settings in the Weapons GUI")
    public GuiSlotItemConfig rerollItem = new GuiSlotItemConfig(
        14,
        "BOOK",
        12347,
        "<color:#ff0000><bold>ᴡᴇᴀᴘᴏɴ ᴄʟᴀѕѕ ʀᴇʀᴏʟʟ ʙᴏᴏᴋ</bold></color>",
        List.of(
            "<white>ᴜѕᴇᴅ ᴛᴏ ʀᴇʀᴏʟʟ ʏᴏᴜʀ ᴀѕѕɪɢɴᴇᴅ</white>",
            "<white>  ᴡᴇᴀᴘᴏɴ.</white>",
            "<white>ᴄᴏɴѕᴜᴍᴇ ɪɴ ʜᴀɴᴅ ᴛᴏ ᴏᴘᴇɴ</white>",
            "<white>  ʀᴇʀᴏʟʟ ᴍᴇɴᴜ.</white>",
            "",
            "<white>ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄʀᴀꜰᴛɪɴɢ</white>",
            "<white>  ʀᴇᴄɪᴘᴇ!</white>"
        ),
        ""
    );

    @Comment("Weapons display items in the GUI mapped by weapon key (trident, bow, shield, crossbow, sword, axe, spear, trident2, crossbow2, mace, armors)")
    public Map<String, GuiSlotItemConfig> weapons = defaultWeaponsMap();

    private static Map<String, GuiSlotItemConfig> defaultWeaponsMap() {
        final Map<String, GuiSlotItemConfig> map = new HashMap<>();

        map.put("sword", new GuiSlotItemConfig(
            20,
            "NETHERITE_SWORD",
            0,
            "<color:#ff0000> <bold>ѕᴡᴏʀᴅ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴅᴏ ᴀ 3-ʜɪᴛ ѕᴡᴏʀᴅ ᴄᴏᴍʙᴏ</white></color>",
                "<white>  ᴡɪᴛʜɪɴ 3 ѕᴇᴄᴏɴᴅѕ ᴛᴏ ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ</white>",
                "<white>  ᴅᴇᴀʟ 2x ᴄʀɪᴛ ᴅᴀᴍᴀɢᴇ</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴄᴛɪᴠᴀᴛᴇѕ ᴅᴜᴀʟ ᴡɪᴇʟᴅ</white></color>",
                "<white>  ɢʀᴀɴᴛɪɴɢ +100% ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ</white>",
                "<white>  ᴀɴᴅ ᴅᴜᴀʟ-ʜᴀɴᴅ ѕᴛʀɪᴋᴇѕ</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("axe", new GuiSlotItemConfig(
            21,
            "NETHERITE_AXE",
            0,
            "<color:#ff0000> <bold>ᴀxᴇ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴅᴏ ᴀ 3 ᴄʀɪᴛ ʜɪᴛѕ ᴛᴏ ᴀɴ</white></color>",
                "<white>  ᴇɴᴇᴍʏ ᴀɴᴅ ѕᴛᴜɴ ᴛʜᴇᴍ ꜰᴏʀ 1</white>",
                "<white>  ѕᴇᴄᴏɴᴅ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴍᴀʀᴋѕ ᴛᴀʀɢᴇᴛ ꜰᴏʀ 10 ѕᴇᴄᴏɴᴅѕ</white></color>",
                "<white>  ѕᴛᴏʀɪɴɢ 100% ᴅᴀᴍᴀɢᴇ ᴀɴᴅ</white>",
                "<white>  ᴅᴇᴛᴏɴᴀᴛɪɴɢ ꜰᴏʀ 150% ʙᴜʀѕᴛ</white>",
                "<white>  ᴏꜰ ᴅᴀᴍᴀɢᴇ</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("trident", new GuiSlotItemConfig(
            22,
            "TRIDENT",
            0,
            "<color:#ff0000> <bold>ᴛʀɪᴅᴇɴᴛ ᴏꜰ ᴘᴏѕᴇɪᴅᴏɴ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 3 ʜɪᴛѕ ѕᴜᴍᴍᴏɴѕ ʟɪɢʜᴛɴɪɴɢ</white></color>",
                "<white>  ᴅᴇᴀʟɪɴɢ 2.0x ᴅᴀᴍᴀɢᴇ + 3.0</white>",
                "<white>  ʙᴏɴᴜѕ ᴅᴀᴍᴀɢᴇ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴜɴʟᴇᴀѕʜᴇѕ 9 ᴛʜʀᴜѕᴛ ѕᴛʀɪᴋᴇѕ</white></color>",
                "<white>  (5.0 ᴅᴍɢ) ᴡɪᴛʜ ᴘᴀʀᴛɪᴄʟᴇ ѕᴜʀɢᴇѕ</white>",
                "<white>  ɪɴ ᴀ 4-ʙʟᴏᴄᴋ ᴢᴏɴᴇ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("bow", new GuiSlotItemConfig(
            23,
            "BOW",
            0,
            "<color:#ff0000> <bold>ʙᴏᴡ ᴏꜰ ᴀʀᴛᴇᴍɪѕ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 2 ʜɪᴛѕ, ɴᴇxᴛ ᴀʀʀᴏᴡ</white></color>",
                "<white>  ᴛʀᴀᴘѕ ᴛᴀʀɢᴇᴛ ɪɴ ᴄᴏʙᴡᴇʙѕ</white>",
                "<white>  ꜰᴏʀ 5ѕ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴘʀɪᴍᴇѕ 3 ʙᴇᴀᴍ ѕʜᴏᴛѕ</white></color>",
                "<white>  (ꜰᴜʟʟ ᴅʀᴀᴡ ꜰɪʀᴇѕ ᴀ 20-ʙʟᴏᴄᴋ</white>",
                "<white>  ʟᴀѕᴇʀ ᴅᴇᴀʟɪɴɢ 8.0 ᴅᴍɢ).</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("crossbow", new GuiSlotItemConfig(
            24,
            "CROSSBOW",
            0,
            "<color:#ff0000> <bold>ᴄʀᴏѕѕʙᴏᴡ ᴏꜰ ᴀᴘᴏʟʟᴏ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇᴠᴇʀʏ 3 ʜɪᴛѕ, ɴᴇxᴛ ᴄʀᴏѕѕʙᴏᴡ</white></color>",
                "<white>  ѕʜᴏᴛ ᴅᴇᴀʟѕ 2.0x ᴅᴀᴍᴀɢᴇ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴘʀɪᴍᴇѕ ɴᴇxᴛ ᴀʀʀᴏᴡ ᴛᴏ ꜰʀᴇᴇᴢᴇ</white></color>",
                "<white>  & ɪᴍᴍᴏʙɪʟɪᴢᴇ ᴛᴀʀɢᴇᴛ ꜰᴏʀ 5ѕ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("spear", new GuiSlotItemConfig(
            29,
            "DIAMOND_SPEAR",
            0,
            "<color:#ff0000> <bold>ѕᴘᴇᴀʀ ᴏꜰ ᴀᴄʜɪʟʟᴇѕ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ʙᴏᴏѕᴛѕ ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ ᴛᴏ ѕᴡᴏʀᴅ</white></color>",
                "<white>  ѕᴘᴇᴇᴅ (1.6) ᴀɴᴅ ᴅᴇᴀʟѕ +2.0</white>",
                "<white>  ʙᴏɴᴜѕ ᴅᴀᴍᴀɢᴇ ᴏɴ ѕᴛʀɪᴋᴇѕ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ѕᴘᴇᴀʀ &</white></color>",
                "<white>  ʀᴇᴍᴏᴠᴇѕ ʜᴜɴɢᴇʀ ᴄᴏɴѕᴜᴍᴘᴛɪᴏɴ</white>",
                "<white>  ᴅᴜʀɪɴɢ ʟᴜɴɢᴇѕ ꜰᴏʀ 15ѕ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("trident2", new GuiSlotItemConfig(
            30,
            "TRIDENT",
            0,
            "<color:#ff0000> <bold>ᴛʜᴜɴᴅᴇʀѕᴛᴏʀᴍ ᴛʀɪᴅᴇɴᴛ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴍᴀᴛᴄʜᴇѕ ѕᴡᴏʀᴅ ᴀᴛᴛᴀᴄᴋ ѕᴘᴇᴇᴅ</white></color>",
                "<white>  (1.6) ᴀɴᴅ ѕᴛᴜɴѕ ʙʟᴏᴄᴋɪɴɢ</white>",
                "<white>  ѕʜɪᴇʟᴅѕ ꜰᴏʀ 5ѕ ᴏɴ ʜɪᴛ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴜᴍᴍᴏɴѕ ᴀ 15ѕ ᴛʜᴜɴᴅᴇʀѕᴛᴏʀᴍ</white></color>",
                "<white>  ᴡʜᴇʀᴇ ᴇᴠᴇʀʏ ᴄʀɪᴛɪᴄᴀʟ ѕᴛʀɪᴋᴇ</white>",
                "<white>  ѕᴛʀɪᴋᴇѕ ᴛᴀʀɢᴇᴛ ᴡɪᴛʜ ʟɪɢʜᴛɴɪɴɢ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("crossbow2", new GuiSlotItemConfig(
            31,
            "CROSSBOW",
            0,
            "<color:#ff0000> <bold>ᴄʀᴏѕѕʙᴏᴡ2</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀʀʀᴏws ʙʏᴘᴀѕѕ ѕʜɪᴇʟᴅ ʙʟᴏᴄᴋѕ</white></color>",
                "<white>  ᴀɴᴅ ѕᴇᴛѕ ᴛᴀʀɢᴇᴛ ᴏɴ ꜰɪʀᴇ</white>",
                "<white>  ꜰᴏʀ 5ѕ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ᴄʀᴏѕѕʙᴏᴡ &</white></color>",
                "<white>  ɢʀᴀɴᴛѕ ᴘᴏᴡᴇʀ v & ǫᴜɪᴄᴋ</white>",
                "<white>  ᴄʜᴀʀɢᴇ v ꜰᴏʀ 15ѕ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("mace", new GuiSlotItemConfig(
            32,
            "MACE",
            0,
            "<color:#ff0000> <bold>ᴍᴀᴄᴇ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴍᴀѕʜ ᴀᴛᴛᴀᴄᴋ ᴄᴏᴏʟᴅᴏᴡɴ ɪѕ</white></color>",
                "<white>  ʀᴇᴅᴜᴄᴇᴅ ʙʏ 50% ᴏɴ</white>",
                "<white>  ѕᴍᴀѕʜ ѕᴛʀɪᴋᴇѕ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴀᴜᴛᴏ-ᴇɴᴄʜᴀɴᴛѕ ᴍᴀᴄᴇ &</white></color>",
                "<white>  ɢʀᴀɴᴛѕ ᴢᴇʀᴏ ѕᴍᴀѕʜ ᴄᴏᴏʟᴅᴏᴡɴ</white>",
                "<white>  ꜰᴏʀ 15ѕ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("armors", new GuiSlotItemConfig(
            33,
            "DIAMOND_CHESTPLATE",
            0,
            "<color:#ff0000> <bold>ᴀʀᴍᴏʀ ᴍᴀѕᴛᴇʀ (ɢᴇᴀʀ ѕᴇᴛ)</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ᴇǫᴜɪᴘᴘɪɴɢ ʙᴀѕᴇ ᴀʀᴍᴏʀ ᴘɪᴇᴄᴇѕ</white></color>",
                "<white>  ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ ᴜᴘɢʀᴀᴅᴇѕ ᴛʜᴇᴍ</white>",
                "<white>  ᴛᴏ ѕᴜᴘᴇʀɪᴏʀ ᴀʀᴍᴏʀ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ɢʀᴀɴᴛѕ 100% ᴋɴᴏᴄᴋʙᴀᴄᴋ</white></color>",
                "<white>  ɪᴍᴍᴜɴɪᴛʏ & ʙᴏᴏѕᴛѕ ɢᴏʟᴅᴇɴ</white>",
                "<white>  ᴀᴘᴘʟᴇ ᴀʙѕᴏʀᴘᴛɪᴏɴ ꜰᴏʀ 20ѕ.</white>",
                "<dark_gray>  ᴄᴏᴏʟᴅᴏᴡɴ: 60 ѕᴇᴄᴏɴᴅѕ</dark_gray>",
                "",
                "<dark_gray>ѕᴛᴀᴛᴜѕ: {status}</dark_gray>"
            )
        ));

        map.put("shield", new GuiSlotItemConfig(
            40,
            "SHIELD",
            0,
            "<color:#ff0000> <bold>ѕʜɪᴇʟᴅ ᴏꜰ ᴀᴛʜᴇɴᴀ</bold></color>",
            List.of(
                "<color:#ff0000> <bold>ᴘᴀѕѕɪᴠᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕʜɪᴇʟᴅ ᴅɪѕᴀʙʟᴇ/ʙʀᴇᴀᴋ</white></color>",
                "<white>  ɢʀᴀɴᴛѕ 20% ᴅᴀᴍᴀɢᴇ</white>",
                "<white>  ʀᴇᴅᴜᴄᴛɪᴏɴ ꜰᴏʀ 5ѕ.</white>",
                "",
                "<color:#ff0000> <bold>ᴜʟᴛɪᴍᴀᴛᴇ</bold></color>",
                "<color:#ff0000>▪ <white>ѕᴘᴀᴡɴѕ ʙᴜʙʙʟᴇ ʙᴀʀʀɪᴇʀ</white></color>",
                "<white>  ɢʀᴀɴᴛɪɴɢ 100% ɪɴᴠᴜʟɴᴇʀᴀʙɪʟɪᴛʏ</white>",
                "<white>  ꜰᴏʀ 15ѕ.</white>",
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
