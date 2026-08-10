package lunatech.strength.listener.player;

import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
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

/**
 * Listener that handles consumption of physical strength items on right-click.
 */
public final class StrengthConsumeListener implements Listener {
    private final StrengthService strengthService;

    public StrengthConsumeListener(@NotNull StrengthService strengthService) {
        this.strengthService = strengthService;
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

        final Integer strengthAmount = meta.getPersistentDataContainer().get(PDCKeys.ITEM_STRENGTH, PersistentDataType.INTEGER);
        if (strengthAmount == null) {
            return;
        }

        // Cancel the event to prevent any default block placing/interaction or projectile throwing
        event.setCancelled(true);

        final Player player = event.getPlayer();

        // Consume one item from the stack
        item.setAmount(item.getAmount() - 1);

        // Add strength to player's base
        final int currentStrength = strengthService.getStrength(player);
        strengthService.setStrength(player, currentStrength + strengthAmount);

        // Play visual and audio effects
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1.0, 0), 12, 0.5, 0.5, 0.5, 0.1);

        player.sendMessage(
            ColorParser.of("<green>You consumed a Strength Shard and gained +<amount> Strength!</green>")
                .with("amount", String.valueOf(strengthAmount))
                .build()
        );
    }
}
