package lunatech.strength.task;

import lunatech.strength.config.PluginConfig.ShieldSettings;
import lunatech.strength.listener.player.ShieldAbilityListener;
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
 * Task that manages the active Shield Ultimate: spawning the visual bubble ItemDisplay,
 * locking the spawn rotation to prevent tilting, adding it as a passenger on the player for
 * zero movement latency/lag, and removing it upon expiration.
 */
public final class ShieldUltimateTask extends BukkitRunnable {
    private final Player player;
    private final ShieldSettings settings;
    private final int durationTicks;
    private int elapsedTicks = 0;
    private ItemDisplay bubbleEntity = null;

    public ShieldUltimateTask(@NotNull Player player, @NotNull ShieldSettings settings) {
        this.player = player;
        this.settings = settings;
        this.durationTicks = settings.ultimateDurationTicks;
        
        // Secure active status immediately on instantiation
        ShieldAbilityListener.shieldUltimateActive.put(player.getUniqueId(), true);
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || elapsedTicks >= durationTicks) {
            cleanup();
            cancel();
            return;
        }

        // 1. Spawning the visual display bubble (Tick 0)
        if (elapsedTicks == 0) {
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.0f, 0.8f);

            try {
                final Material mat = Material.valueOf(settings.bubbleMaterial);
                
                // Spawn at player's location with pitch locked to 0.0 to prevent vertical tilting
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
                    
                    // Center the bubble horizontally/vertically relative to the player mounting point (usually head level)
                    display.setTransformation(new Transformation(
                        new Vector3f(settings.bubbleOffsetX, settings.bubbleOffsetY, settings.bubbleOffsetZ),
                        new Quaternionf(),
                        new Vector3f(1.0f, 1.0f, 1.0f),
                        new Quaternionf()
                    ));
                });

                // Set passenger so the display smoothly follows player client-side with absolute zero latency
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
            player.sendMessage(io.github.milkdrinkers.colorparser.paper.ColorParser.of("<red>Your Shield Ultimate bubble has expired!</red>").build());
            player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f);
        }
    }
}
