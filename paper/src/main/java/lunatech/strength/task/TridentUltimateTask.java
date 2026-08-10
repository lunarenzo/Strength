package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.TridentSettings;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Task that manages the active Trident Ultimate ability: moving the player, checking collisions,
 * spawning/tracking client-side water block changes, and restoring them.
 */
public final class TridentUltimateTask extends BukkitRunnable {
    private final Player player;
    private final ArmorStand vehicle;
    private final TridentSettings settings;
    private final int durationTicks;
    private int elapsedTicks = 0;

    private final BlockData waterBlockData = Bukkit.createBlockData(Material.WATER);
    private final List<TrailBlock> trailBlocks = new ArrayList<>();

    private record TrailBlock(Location location, BlockData originalData, long restoreTick) {}

    public TridentUltimateTask(@NotNull Player player, @NotNull ArmorStand vehicle, @NotNull TridentSettings settings) {
        this.player = player;
        this.vehicle = vehicle;
        this.settings = settings;
        this.durationTicks = settings.ultimateDurationTicks;
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.getVehicle() != vehicle || elapsedTicks >= durationTicks) {
            cancelAndCleanup();
            return;
        }

        // Calculate direction vector, ignoring upward vertical look to prevent flying
        final Location playerLoc = player.getLocation();
        Vector dir = playerLoc.getDirection();
        if (dir.getY() > 0) {
            dir.setY(0);
        }
        if (dir.lengthSquared() > 0) {
            dir.normalize();
        }
        dir.multiply(settings.ultimateSpeed);

        // Calculate next location
        final Location currentLoc = vehicle.getLocation();
        final Location nextLoc = currentLoc.clone().add(dir);

        // Check for solid block collisions at vehicle location (feet and head height)
        if (isSolidBlock(nextLoc) || isSolidBlock(nextLoc.clone().add(0, 1, 0))) {
            player.sendMessage(ColorParser.of("<red>Collided with a wall! Ability ended.</red>").build());
            cancelAndCleanup();
            return;
        }

        // Teleport vehicle (player will move with it client-side)
        vehicle.teleport(nextLoc);

        // Generate water trail behind player
        final Vector backDir = dir.clone().normalize().multiply(-1.2);
        final Location trailLoc1 = nextLoc.clone().add(backDir);
        final Location trailLoc2 = trailLoc1.clone().add(0, 1, 0);

        spawnWaterBlock(trailLoc1);
        spawnWaterBlock(trailLoc2);

        // Handle scheduled restores
        restoreExpiredBlocks();

        elapsedTicks++;
    }

    private boolean isSolidBlock(Location loc) {
        return loc.getBlock().getType().isSolid();
    }

    private void spawnWaterBlock(Location loc) {
        // Only override non-solid blocks (e.g. air, grass)
        if (loc.getBlock().getType().isSolid()) {
            return;
        }

        // Prevent duplicate trail logging for the same location
        for (TrailBlock tb : trailBlocks) {
            if (tb.location.getBlockX() == loc.getBlockX() &&
                tb.location.getBlockY() == loc.getBlockY() &&
                tb.location.getBlockZ() == loc.getBlockZ()) {
                return;
            }
        }

        final Location blockLoc = loc.getBlock().getLocation();
        final BlockData originalData = blockLoc.getBlock().getBlockData();

        // Register trail block and fake change for all online players
        trailBlocks.add(new TrailBlock(blockLoc, originalData, elapsedTicks + 30)); // Restore after 1.5 seconds (30 ticks)
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendBlockChange(blockLoc, waterBlockData);
        }
    }

    private void restoreExpiredBlocks() {
        trailBlocks.removeIf(tb -> {
            if (elapsedTicks >= tb.restoreTick) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendBlockChange(tb.location, tb.originalData);
                }
                return true;
            }
            return false;
        });
    }

    private void cancelAndCleanup() {
        cancel();
        
        // Restore passengers and remove invisible vehicle
        if (player.isOnline() && player.getVehicle() == vehicle) {
            vehicle.removePassenger(player);
        }
        vehicle.remove();

        // Instantly restore all faked water blocks back to original state
        for (TrailBlock tb : trailBlocks) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendBlockChange(tb.location, tb.originalData);
            }
        }
        trailBlocks.clear();
    }
}
