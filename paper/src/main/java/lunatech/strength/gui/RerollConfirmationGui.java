package lunatech.strength.gui;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.ChestGuiSettings;
import lunatech.strength.config.PluginConfig.RerollDialogSettings;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import lunatech.strength.utility.ItemResolver;
import lunatech.strength.utility.Logger;
import lunatech.strength.utility.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Manager responsible for opening the Weapon Reroll Confirmation interface via Paper Dialog API or native Chest Inventory.
 */
public final class RerollConfirmationGui {

    private RerollConfirmationGui() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void open(@NotNull Strength plugin, @NotNull Player player) {
        final RerollDialogSettings dialogSettings = plugin.getConfigHandler().getConfig().rerollDialog;

        if ("DIALOG".equalsIgnoreCase(dialogSettings.mode)) {
            if (openPaperDialog(plugin, player, dialogSettings)) {
                return;
            }
        }

        // Default or Fallback to Native Chest GUI
        openChestGui(plugin, player, dialogSettings);
    }

    private static boolean openPaperDialog(@NotNull Strength plugin, @NotNull Player player, @NotNull RerollDialogSettings dialogSettings) {
        try {
            final MiniMessage mm = MiniMessage.miniMessage();
            final Component title = mm.deserialize(dialogSettings.title);
            final Component message = mm.deserialize(dialogSettings.message);
            final Component confirmText = mm.deserialize(dialogSettings.confirmButton);
            final Component confirmTooltip = mm.deserialize(dialogSettings.confirmTooltip);
            final Component cancelText = mm.deserialize(dialogSettings.cancelButton);
            final Component cancelTooltip = mm.deserialize(dialogSettings.cancelTooltip);

            final io.papermc.paper.registry.data.dialog.ActionButton yesButton = io.papermc.paper.registry.data.dialog.ActionButton.builder(confirmText)
                .tooltip(confirmTooltip)
                .action(io.papermc.paper.registry.data.dialog.action.DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player targetPlayer) {
                        executeReroll(plugin, targetPlayer);
                    }
                }, ClickCallback.Options.builder().uses(1).build()))
                .build();

            final io.papermc.paper.registry.data.dialog.ActionButton noButton = io.papermc.paper.registry.data.dialog.ActionButton.builder(cancelText)
                .tooltip(cancelTooltip)
                .action(io.papermc.paper.registry.data.dialog.action.DialogAction.customClick((view, audience) -> {
                    // Canceled cleanly
                }, ClickCallback.Options.builder().uses(1).build()))
                .build();

            final io.papermc.paper.dialog.Dialog dialog = io.papermc.paper.dialog.Dialog.create(builder -> builder.empty()
                .base(io.papermc.paper.registry.data.dialog.DialogBase.builder(title)
                    .body(List.<io.papermc.paper.registry.data.dialog.body.DialogBody>of(
                        io.papermc.paper.registry.data.dialog.body.DialogBody.plainMessage(message),
                        io.papermc.paper.registry.data.dialog.body.DialogBody.item(plugin.getStrengthService().createRerollItem()).build()
                    ))
                    .build()
                )
                .type(io.papermc.paper.registry.data.dialog.type.DialogType.confirmation(yesButton, noButton))
            );

            player.showDialog(dialog);
            return true;
        } catch (Throwable t) {
            Logger.get().debug("Paper Dialog API not available, falling back to Chest GUI: {}", t.getMessage());
            return false;
        }
    }

    private static void openChestGui(@NotNull Strength plugin, @NotNull Player player, @NotNull RerollDialogSettings dialogSettings) {
        final MiniMessage mm = MiniMessage.miniMessage();
        final ChestGuiSettings guiSettings = dialogSettings.gui;

        final Component title = mm.deserialize(dialogSettings.title);
        final int rows = Math.max(1, Math.min(6, guiSettings.rows));
        final int totalSlots = rows * 9;

        final RerollConfirmationHolder holder = new RerollConfirmationHolder();
        final Inventory inv = Bukkit.createInventory(holder, totalSlots, title);
        holder.setInventory(inv);

        // 1. Filler background items
        ItemStack fillerItem = ItemResolver.resolveItemStack(guiSettings.fillerMaterial, plugin.getStrengthService());
        if (fillerItem == null) {
            fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        fillerItem.editMeta(meta -> meta.displayName(Component.empty()));

        for (int i = 0; i < totalSlots; i++) {
            inv.setItem(i, fillerItem.clone());
        }

        // 2. Confirm (YES) Button
        ItemStack confirmItem = ItemResolver.resolveItemStack(guiSettings.confirmMaterial, plugin.getStrengthService());
        if (confirmItem == null) {
            confirmItem = new ItemStack(Material.LIME_CONCRETE);
        }
        confirmItem.editMeta(meta -> {
            meta.displayName(mm.deserialize(dialogSettings.confirmButton));
            meta.lore(List.of(mm.deserialize("<gray>Click to confirm and consume reroll book.</gray>")));
        });

        if (guiSettings.confirmSlot >= 0 && guiSettings.confirmSlot < totalSlots) {
            inv.setItem(guiSettings.confirmSlot, confirmItem);
        }

        // 3. Info / Preview Item
        final ItemStack previewItem = plugin.getStrengthService().createRerollItem();
        previewItem.editMeta(meta -> {
            meta.displayName(mm.deserialize(dialogSettings.title));
            meta.lore(List.of(mm.deserialize(dialogSettings.message)));
        });

        if (guiSettings.previewSlot >= 0 && guiSettings.previewSlot < totalSlots) {
            inv.setItem(guiSettings.previewSlot, previewItem);
        }

        // 4. Cancel (NO) Button
        ItemStack cancelItem = ItemResolver.resolveItemStack(guiSettings.cancelMaterial, plugin.getStrengthService());
        if (cancelItem == null) {
            cancelItem = new ItemStack(Material.RED_CONCRETE);
        }
        cancelItem.editMeta(meta -> {
            meta.displayName(mm.deserialize(dialogSettings.cancelButton));
            meta.lore(List.of(mm.deserialize("<gray>Click to cancel.</gray>")));
        });

        if (guiSettings.cancelSlot >= 0 && guiSettings.cancelSlot < totalSlots) {
            inv.setItem(guiSettings.cancelSlot, cancelItem);
        }

        player.openInventory(inv);
    }

    public static void executeReroll(@NotNull Strength plugin, @NotNull Player player) {
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
