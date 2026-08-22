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

        // Consume one item from the stack
        item.setAmount(item.getAmount() - 1);

        // Visual and audio feedback
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
}
