package lunatech.strength.task;

import lunatech.strength.config.PluginConfig.TridentSettings;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
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

    private Location lastBlockLoc = null;
    private final List<WaveColumn> waveColumns = new ArrayList<>();

    private record WaveColumn(
        Location lower,
        Location upper,
        BlockData originalLower,
        BlockData originalUpper
    ) {}

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

        // Calculate strictly horizontal direction vector
        final Location playerLoc = player.getLocation();
        Vector dir = playerLoc.getDirection();
        dir.setY(0); // Ensure velocity is strictly horizontal
        if (dir.lengthSquared() > 0) {
            dir.normalize();
        }
        dir.multiply(settings.ultimateSpeed);

        // Next location at current Y
        final Location currentLoc = vehicle.getLocation();
        final Location targetLoc = currentLoc.clone().add(dir);

        // Dynamic terrain scanning
        final double currentTerrainY = currentLoc.getY() - 3.0;
        final double nextTerrainY = getTerrainY(targetLoc);

        // Height change verification (wall / steep cliff check)
        if (nextTerrainY - currentTerrainY > 1.0) {
            player.sendMessage(ColorParser.of("<red>Collided with a wall! Ability ended.</red>").build());
            cancelAndCleanup();
            return;
        }

        // Ceiling/Clearance check
        final Location headCheckLoc = targetLoc.clone();
        headCheckLoc.setY(nextTerrainY + 4.0);
        if (isSolidBlock(headCheckLoc)) {
            player.sendMessage(ColorParser.of("<red>Collided with a ceiling! Ability ended.</red>").build());
            cancelAndCleanup();
            return;
        }

        // Update vehicle location (3 blocks above ground/water surface)
        final Location nextLoc = targetLoc.clone();
        nextLoc.setY(nextTerrainY + 3.0);
        vehicle.teleport(nextLoc);

        // Track block crossing to update the 7-block water trail
        final Location currentBlockLoc = targetLoc.getBlock().getLocation();
        if (lastBlockLoc == null || lastBlockLoc.getBlockX() != currentBlockLoc.getBlockX() || lastBlockLoc.getBlockZ() != currentBlockLoc.getBlockZ()) {
            lastBlockLoc = currentBlockLoc;

            // Generate faked water column directly under the vehicle (Y-1 and Y-2)
            final Location trailLower = currentBlockLoc.clone();
            trailLower.setY(nextTerrainY + 1.0);
            final Location trailUpper = currentBlockLoc.clone();
            trailUpper.setY(nextTerrainY + 2.0);

            // Save original block states
            final BlockData origLower = trailLower.getBlock().getBlockData();
            final BlockData origUpper = trailUpper.getBlock().getBlockData();

            // Insert new column at the front of the queue
            waveColumns.add(0, new WaveColumn(trailLower, trailUpper, origLower, origUpper));

            // Clean up oldest column to keep trail length strictly 7 blocks
            if (waveColumns.size() > 7) {
                final WaveColumn oldest = waveColumns.remove(7);
                restoreColumn(oldest);
            }

            // Update water levels of active columns to simulate flowing wave tail
            updateWaveLevels();
        }

        elapsedTicks++;
    }

    private double getTerrainY(Location loc) {
        final Location scan = loc.clone();
        final int minHeight = scan.getWorld().getMinHeight();
        final int maxHeight = scan.getWorld().getMaxHeight();

        // Start scanning from current height down
        int startY = Math.min(maxHeight, Math.max(minHeight, scan.getBlockY()));
        scan.setY(startY);

        while (scan.getY() > minHeight) {
            if (isGround(scan.getBlock())) {
                return scan.getY();
            }
            scan.subtract(0, 1, 0);
        }
        return loc.getY() - 3.0; // Fallback
    }

    private boolean isGround(Block block) {
        return block.getType().isSolid() || block.getType() == Material.WATER;
    }

    private boolean isSolidBlock(Location loc) {
        return loc.getBlock().getType().isSolid();
    }

    private void updateWaveLevels() {
        for (int i = 0; i < waveColumns.size(); i++) {
            final WaveColumn col = waveColumns.get(i);

            // Lower block: Full source water (Level 0)
            final BlockData lowerWater = Bukkit.createBlockData(Material.WATER);
            if (lowerWater instanceof Levelled l) {
                l.setLevel(0);
            }

            // Upper block: Flowing water (Levelled from 0 to 6 based on trail distance)
            final BlockData upperWater = Bukkit.createBlockData(Material.WATER);
            if (upperWater instanceof Levelled l) {
                l.setLevel(i); // i is from 0 to 6
            }

            // Send packet updates to all online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendBlockChange(col.lower, lowerWater);
                p.sendBlockChange(col.upper, upperWater);
            }
        }
    }

    private void restoreColumn(WaveColumn col) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendBlockChange(col.lower, col.originalLower);
            p.sendBlockChange(col.upper, col.originalUpper);
        }
    }

    private void cancelAndCleanup() {
        cancel();

        // Remove passenger
        if (player.isOnline() && player.getVehicle() == vehicle) {
            vehicle.removePassenger(player);
        }
        vehicle.remove();

        // Restore all remaining active trail blocks
        for (WaveColumn col : waveColumns) {
            restoreColumn(col);
        }
        waveColumns.clear();
    }
}
