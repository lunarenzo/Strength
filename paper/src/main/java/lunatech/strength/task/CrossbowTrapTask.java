package lunatech.strength.task;

import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.listener.player.CrossbowAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task that manages a planted Crossbow Ultimate Trap: detecting when a victim triggers it,
 * rendering particle boundaries, and locking the victim inside with physical pulls/knockbacks.
 */
public final class CrossbowTrapTask extends BukkitRunnable {
    private final Player shooter;
    private final Location anchorLoc;
    private final CrossbowConfig settings;
    
    private boolean sprung = false;
    private UUID victimUuid = null;
    private int trapTicksLeft = 0;

    public CrossbowTrapTask(@NotNull Player shooter, @NotNull Location anchorLoc, @NotNull CrossbowConfig settings) {
        this.shooter = shooter;
        this.anchorLoc = anchorLoc;
        this.settings = settings;
    }

    @Override
    public void run() {
        // Draw outline of the trap center to show it is active/planted
        if (!sprung) {
            // Spawn subtle portal sparks at the anchor block to represent the trap mechanism
            anchorLoc.getWorld().spawnParticle(Particle.PORTAL, anchorLoc, 3, 0.2, 0.2, 0.2, 0.05);

            // Find target player in 3-block radius
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getUniqueId().equals(shooter.getUniqueId()) || player.isDead() || !player.isOnline()) {
                    continue;
                }
                
                if (player.getWorld().equals(anchorLoc.getWorld()) && player.getLocation().distance(anchorLoc) <= settings.trapTriggerRadius) {
                    // Spring the trap!
                    sprung = true;
                    victimUuid = player.getUniqueId();
                    trapTicksLeft = settings.trapDurationSeconds * 20;

                    // Add victim to active global tether list for teleport checks
                    CrossbowAbilityListener.trappedPlayers.put(victimUuid, anchorLoc);

                    // Sound & Message effects
                    anchorLoc.getWorld().playSound(anchorLoc, Sound.BLOCK_CHAIN_BREAK, 1.0f, 0.8f);
                    player.sendMessage(ColorParser.of(settings.trapTriggeredVictimMessage).build());
                    break;
                }
            }
        } else {
            // Trap is active and sprung
            final Player victim = Bukkit.getPlayer(victimUuid);
            if (victim == null || !victim.isOnline() || victim.isDead() || trapTicksLeft <= 0) {
                cleanup();
                cancel();
                return;
            }

            // Draw trap visual boundary
            if (trapTicksLeft % 2 == 0) {
                drawOutline();
            }

            // Check if victim is trying to escape past the max distance boundary
            if (victim.getWorld().equals(anchorLoc.getWorld())) {
                final double currentDist = victim.getLocation().distance(anchorLoc);
                if (currentDist > settings.trapMaxDistance) {
                    // Snap / Pull the victim back towards the anchor coordinate
                    final Vector pull = anchorLoc.toVector().subtract(victim.getLocation().toVector());
                    if (pull.lengthSquared() > 0) {
                        pull.normalize();
                    }
                    
                    // Propel the victim back and upwards
                    pull.multiply(1.8).setY(0.4);
                    victim.setVelocity(pull);
                    
                    // Visual/Sound feedback
                    victim.getWorld().playSound(victim.getLocation(), Sound.BLOCK_CHAIN_FALL, 1.0f, 1.0f);
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation(), 15, 0.2, 0.2, 0.2, 0.1);
                }
            }

            trapTicksLeft--;
        }
    }

    private void drawOutline() {
        final double minX = anchorLoc.getX() - 1.5;
        final double maxX = anchorLoc.getX() + 1.5;
        final double minY = anchorLoc.getY() - 1.5;
        final double maxY = anchorLoc.getY() + 1.5;
        final double minZ = anchorLoc.getZ() - 1.5;
        final double maxZ = anchorLoc.getZ() + 1.5;

        final org.bukkit.World world = anchorLoc.getWorld();
        
        // Spawn particles along the corners & frame edges
        for (double y = minY; y <= maxY; y += 0.5) {
            world.spawnParticle(Particle.CRIT, minX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, maxX, y, minZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, minX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, maxX, y, maxZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (double x = minX; x <= maxX; x += 0.5) {
            world.spawnParticle(Particle.CRIT, x, minY, minZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, x, maxY, minZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, x, minY, maxZ, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, x, maxY, maxZ, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (double z = minZ; z <= maxZ; z += 0.5) {
            world.spawnParticle(Particle.CRIT, minX, minY, z, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, minX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, maxX, minY, z, 1, 0.0, 0.0, 0.0, 0.0);
            world.spawnParticle(Particle.CRIT, maxX, maxY, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private void cleanup() {
        if (victimUuid != null) {
            CrossbowAbilityListener.trappedPlayers.remove(victimUuid);
        }
    }
}
