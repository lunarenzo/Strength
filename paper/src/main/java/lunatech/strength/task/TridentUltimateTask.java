package lunatech.strength.task;

import lunatech.strength.config.TridentConfig;
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
    private final TridentConfig settings;
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

    public TridentUltimateTask(@NotNull Player player, @NotNull ArmorStand vehicle, @NotNull TridentConfig settings) {
        this.player = player;
        this.vehicle = vehicle;
        this.settings = settings;
        this.durationTicks = settings.ultimateDurationTicks;
    }

    @Override
    public void run() {
        if (!player.isOnline() || player.isDead() || elapsedTicks >= durationTicks) {
            cancelAndCleanup();
            return;
        }

        // Calculate horizontal direction player is facing
        final Vector dir = player.getEyeLocation().getDirection().setY(0);
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
            player.sendMessage(ColorParser.of(settings.ultimateCollisionWallMessage).build());
            cancelAndCleanup();
            return;
        }

        // Ceiling/Clearance check
        final Location headCheckLoc = targetLoc.clone();
        headCheckLoc.setY(nextTerrainY + 4.0);
        if (isSolidBlock(headCheckLoc)) {
            player.sendMessage(ColorParser.of(settings.ultimateCollisionCeilingMessage).build());
            cancelAndCleanup();
            return;
        }

        // Update vehicle location (3 blocks above ground/water surface)
        final Location nextLoc = targetLoc.clone();
        nextLoc.setY(nextTerrainY + 3.0);
        vehicle.teleport(nextLoc);

        // Water Wave rendering (Tick 0, 5, 10, etc.)
        if (elapsedTicks % 5 == 0) {
            spawnWave(nextLoc.clone().subtract(0, 3, 0));
        }

        elapsedTicks++;
    }

    private double getTerrainY(Location loc) {
        final Location scan = loc.clone();
        scan.setY(loc.getY() + 3.0); // scan from upper height

        final int minHeight = scan.getWorld().getMinHeight();
        while (scan.getY() > minHeight) {
            if (scan.getBlock().getType().isSolid() || scan.getBlock().getType() == Material.WATER) {
                return scan.getY();
            }
            scan.subtract(0, 1, 0);
        }
        return loc.getY() - 3.0; // fallback
    }

    private boolean isSolidBlock(Location loc) {
        final Block b = loc.getBlock();
        return b.getType().isSolid() && b.getType() != Material.WATER;
    }

    private void spawnWave(Location center) {
        if (lastBlockLoc != null && lastBlockLoc.getBlockX() == center.getBlockX() && lastBlockLoc.getBlockZ() == center.getBlockZ()) {
            return; // Only spawn if moved to new horizontal block
        }
        lastBlockLoc = center.clone();

        // Calculate perpendicular vectors to render 3-block wide wave
        final Vector dir = player.getEyeLocation().getDirection().setY(0).normalize();
        final Vector ortho = new Vector(-dir.getZ(), 0, dir.getX()).normalize();

        final Location left = center.clone().add(ortho);
        final Location right = center.clone().subtract(ortho);

        renderWaveAt(left);
        renderWaveAt(center);
        renderWaveAt(right);
    }

    private void renderWaveAt(Location loc) {
        final Location lower = loc.clone();
        final Location upper = loc.clone().add(0, 1, 0);

        final BlockData origLower = lower.getBlock().getBlockData();
        final BlockData origUpper = upper.getBlock().getBlockData();

        // Safe check: Only place if air or water (don't overwrite solid blocks)
        if (canOverwrite(lower.getBlock()) && canOverwrite(upper.getBlock())) {
            // Store block states for cleanup
            waveColumns.add(new WaveColumn(lower.clone(), upper.clone(), origLower, origUpper));

            // Set water block states
            final BlockData waterData = Bukkit.createBlockData(Material.WATER, data -> {
                if (data instanceof Levelled levelled) {
                    levelled.setLevel(0); // full block
                }
            });
            lower.getBlock().setBlockData(waterData, false);
            upper.getBlock().setBlockData(waterData, false);

            // Re-apply block changes for nearby players
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(loc.getWorld()) && p.getLocation().distanceSquared(loc) < 2500) {
                    p.sendBlockChange(lower, waterData);
                    p.sendBlockChange(upper, waterData);
                }
            }
        }
    }

    private boolean canOverwrite(Block block) {
        final Material type = block.getType();
        return type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR || type == Material.WATER;
    }

    private void cancelAndCleanup() {
        cancel();

        // Remove passenger vehicle
        vehicle.remove();

        // Restore blocks in chronological order
        for (WaveColumn col : waveColumns) {
            col.lower.getBlock().setBlockData(col.originalLower, false);
            col.upper.getBlock().setBlockData(col.originalUpper, false);

            // Re-sync block updates for clients
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getWorld().equals(col.lower.getWorld()) && p.getLocation().distanceSquared(col.lower) < 2500) {
                    p.sendBlockChange(col.lower, col.originalLower);
                    p.sendBlockChange(col.upper, col.originalUpper);
                }
            }
        }
        waveColumns.clear();
    }
}
