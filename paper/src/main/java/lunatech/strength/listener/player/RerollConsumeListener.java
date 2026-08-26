package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener that handles consumption of physical Weapon Reroll Books on right-click.
 */
public final class RerollConsumeListener implements Listener {
    private final Strength plugin;

    public RerollConsumeListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(@NotNull PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        final ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final Integer isRerollItem = meta.getPersistentDataContainer().get(PDCKeys.ITEM_REROLL, PersistentDataType.INTEGER);
        if (isRerollItem == null || isRerollItem != 1) {
            return;
        }

        // Cancel event to prevent placing or opening blocks
        event.setCancelled(true);

        final Player player = event.getPlayer();

        // Check if PvPManager is active and player is in combat
        if (plugin.getServer().getPluginManager().isPluginEnabled("PvPManager")) {
            if (lunatech.strength.integration.PvPManagerHook.isInCombat(plugin, player)) {
                MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.cannotRerollInCombatMessage);
                return;
            }
        }

        // Check WorldGuard region restrictions
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!lunatech.strength.integration.WorldGuardHook.isRerollAllowed(plugin, player, player.getLocation())) {
                MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.cannotRerollInRegionMessage);
                return;
            }
        }

        // If confirmation dialog is enabled in config, pop up dialog GUI for confirmation
        if (plugin.getConfigHandler().getConfig().rerollDialog.enabled) {
            lunatech.strength.gui.RerollConfirmationGui.open(plugin, player);
            return;
        }

        // Direct consumption if confirmation dialog is disabled
        item.setAmount(item.getAmount() - 1);

        // Visual and audio feedback
        final var weaponSettings = plugin.getConfigHandler().getConfig().weapons;
        if (weaponSettings.consumeSound != null && !weaponSettings.consumeSound.isEmpty() && !weaponSettings.consumeSound.equalsIgnoreCase("NONE")) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(weaponSettings.consumeSound.toUpperCase()), weaponSettings.consumeSoundVolume, weaponSettings.consumeSoundPitch);
            } catch (Exception ignored) {
                player.playSound(player.getLocation(), weaponSettings.consumeSound.toLowerCase(), weaponSettings.consumeSoundVolume, weaponSettings.consumeSoundPitch);
            }
        }
        player.spawnParticle(Particle.ENCHANT, player.getLocation().add(0, 1.0, 0), 20, 0.5, 0.5, 0.5, 0.2);

        // Send consumed message if configured
        MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.consumedRerollBookMessage);

        // Trigger visual weapon reroll task
        new WeaponRollTask(plugin, player).start();
    }
}
