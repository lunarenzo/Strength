package lunatech.strength.gui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.RerollDialogSettings;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import lunatech.strength.utility.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Confirmation dialog GUI shown when consuming a Weapon Reroll Book.
 */
public final class RerollConfirmationGui {

    private RerollConfirmationGui() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void open(@NotNull Strength plugin, @NotNull Player player) {
        final RerollDialogSettings dialogSettings = plugin.getConfigHandler().getConfig().rerollDialog;
        final MiniMessage mm = MiniMessage.miniMessage();

        final Component title = mm.deserialize(dialogSettings.title);

        final Gui gui = Gui.gui()
            .title(title)
            .rows(3)
            .disableAllInteractions()
            .create();

        // 1. Confirm (YES) Button - Slot 11
        final GuiItem yesItem = ItemBuilder.from(Material.LIME_CONCRETE)
            .name(mm.deserialize(dialogSettings.confirmButton))
            .lore(mm.deserialize("<gray>Click to confirm and consume reroll book.</gray>"))
            .asGuiItem(event -> {
                player.closeInventory();
                executeReroll(plugin, player);
            });
        gui.setItem(11, yesItem);

        // 2. Info / Preview Item - Slot 13
        final ItemStack rerollItem = plugin.getStrengthService().createRerollItem();
        final GuiItem previewItem = ItemBuilder.from(rerollItem)
            .name(mm.deserialize(dialogSettings.title))
            .lore(mm.deserialize(dialogSettings.message))
            .asGuiItem(event -> event.setCancelled(true));
        gui.setItem(13, previewItem);

        // 3. Cancel (NO) Button - Slot 15
        final GuiItem noItem = ItemBuilder.from(Material.RED_CONCRETE)
            .name(mm.deserialize(dialogSettings.cancelButton))
            .lore(mm.deserialize("<gray>Click to cancel.</gray>"))
            .asGuiItem(event -> player.closeInventory());
        gui.setItem(15, noItem);

        // Filler items for aesthetic border
        final GuiItem filler = ItemBuilder.from(Material.GRAY_STAINED_GLASS_PANE)
            .name(Component.empty())
            .asGuiItem();
        gui.getFiller().fill(filler);

        gui.open(player);
    }

    private static void executeReroll(@NotNull Strength plugin, @NotNull Player player) {
        // Find and consume 1 Reroll Book from inventory or hand
        ItemStack targetItem = player.getInventory().getItemInMainHand();
        if (!isRerollBook(targetItem)) {
            targetItem = player.getInventory().getItemInOffHand();
            if (!isRerollBook(targetItem)) {
                targetItem = null;
                for (ItemStack invItem : player.getInventory().getContents()) {
                    if (isRerollBook(invItem)) {
                        targetItem = invItem;
                        break;
                    }
                }
            }
        }

        if (targetItem == null) {
            return;
        }

        // Deduct 1 item stack
        targetItem.setAmount(targetItem.getAmount() - 1);

        // Feedback effects
        player.playSound(player.getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.8f, 1.2f);
        player.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1.0, 0), 20, 0.5, 0.5, 0.5, 0.2);

        final StrengthService strengthService = plugin.getStrengthService();
        final List<String> availableWeapons = plugin.getConfigHandler().getConfig().weapons.availableWeapons;
        final String rollStartTitle = plugin.getConfigHandler().getConfig().weapons.rollStartTitle;

        // Send consumed message if configured
        MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.consumedRerollBookMessage);

        // Trigger visual weapon reroll task
        new WeaponRollTask(player, availableWeapons, strengthService, rollStartTitle).runTaskTimer(plugin, 0L, 2L);
    }

    private static boolean isRerollBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        final Integer val = meta.getPersistentDataContainer().get(PDCKeys.ITEM_REROLL, PersistentDataType.INTEGER);
        return val != null && val == 1;
    }
}
