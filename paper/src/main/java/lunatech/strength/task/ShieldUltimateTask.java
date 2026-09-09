package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.ShieldConfig;
import lunatech.strength.listener.player.ShieldAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.UUID;

/**
 * Task managing active Shield Ultimate: spawning visual bubble ItemDisplay,
 * locking spawn pitch to prevent vertical tilting, adding it as passenger on player
 * for zero movement latency, and removing it upon expiration.
 */
public final class ShieldUltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final ShieldConfig.UltimateConfig settings;
    private final int durationTicks;
    private int elapsedTicks = 0;
    private ItemDisplay bubbleEntity = null;

    public ShieldUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull ShieldConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
        this.durationTicks = settings.durationTicks;

        // Secure active status immediately on instantiation
        ShieldAbilityListener.shieldUltimateActive.put(player.getUniqueId(), true);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || elapsedTicks >= (durationTicks * plugin.getStrengthService().getScale())) {
            cleanup();
            cancel();
            return;
        }

        // 1. Spawning visual display bubble (Tick 0)
        if (elapsedTicks == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);

            try {
                final Material mat = Material.valueOf(settings.bubbleMaterial);

                // Spawn at player location with pitch locked to 0.0 to prevent vertical tilting
                final Location spawnLoc = player.getLocation().clone();
                spawnLoc.setPitch(0.0f);

                bubbleEntity = player.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
                    final ItemStack item = new ItemStack(mat, 1);
                    final ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setCustomModelData(settings.bubbleCustomModelData);
                        item.setItemMeta(meta);
                    }
                    display.setItemStack(item);
                    display.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD);

                    // Center bubble horizontally/vertically relative to player head level
                    display.setTransformation(new Transformation(
                        new Vector3f(settings.bubbleOffsetX, settings.bubbleOffsetY, settings.bubbleOffsetZ),
                        new Quaternionf(),
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        new Quaternionf()
                    ));
                });

                // Set passenger so display smoothly follows player client-side with zero latency
                player.addPassenger(bubbleEntity);
            } catch (Exception e) {
                final Location spawnLoc = player.getLocation().clone();
                spawnLoc.setPitch(0.0f);
                bubbleEntity = player.getWorld().spawn(spawnLoc, ItemDisplay.class, display -> {
                    display.setItemStack(new ItemStack(Material.GLASS, 1));
                });
                player.addPassenger(bubbleEntity);
            }
        }

        elapsedTicks++;
    }

    private void cleanup() {
        final UUID uuid = player.getUniqueId();
        ShieldAbilityListener.shieldUltimateActive.remove(uuid);

        if (player.isOnline()) {
            if (bubbleEntity != null) {
                player.removePassenger(bubbleEntity);
            }
        }

        if (bubbleEntity != null) {
            bubbleEntity.remove();
            bubbleEntity = null;
        }

        if (player.isOnline()) {
            if (settings.ultimateExpiredMessage != null && !settings.ultimateExpiredMessage.isBlank()) {
                player.sendMessage(ColorParser.of(settings.ultimateExpiredMessage).build());
            }
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
        }
    }
}
