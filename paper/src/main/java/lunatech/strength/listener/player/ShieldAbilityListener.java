package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.ShieldSettings;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Shield abilities: tracking shield blocks to charge the ultimate,
 * applying 20% passive damage reduction when the shield is on cooldown (stunned),
 * and managing god-mode damage negation and knockback when the ultimate is active.
 */
public final class ShieldAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Concurrent collections to ensure zero memory retention and thread safety
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> shieldUltimateActive = new ConcurrentHashMap<>();

    public ShieldAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamageLowest(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (shieldUltimateActive.getOrDefault(victim.getUniqueId(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamageHighest(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (shieldUltimateActive.getOrDefault(victim.getUniqueId(), false)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamagePassive(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        final UUID uuid = victim.getUniqueId();

        // 2. Passive Ability: 20% Damage Reduction when Shield is Stunned (On Cooldown)
        final String assigned = strengthService.getAssignedWeapon(victim);
        if ("shield".equalsIgnoreCase(assigned)) {
            if (victim.hasCooldown(Material.SHIELD)) {
                // Reduce incoming damage by 20%
                event.setDamage(event.getDamage() * 0.80);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onEntityDamageByEntityLowest(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        final UUID uuid = victim.getUniqueId();

        // 1. Ultimate Active: God Mode Knockback (runs even if event is already cancelled)
        if (shieldUltimateActive.getOrDefault(uuid, false)) {
            event.setCancelled(true);

            // Push the attacker back and up
            if (event.getDamager() instanceof org.bukkit.entity.LivingEntity attacker) {
                final Vector pushDir = attacker.getLocation().toVector().subtract(victim.getLocation().toVector());
                if (pushDir.lengthSquared() > 0) {
                    pushDir.normalize();
                }
                pushDir.multiply(1.0).setY(0.3); // Push away and upwards
                attacker.setVelocity(pushDir);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.0f, 1.0f);
            }
            return;
        }

        // 2. Shield Block Charge Tracking
        final String assigned = strengthService.getAssignedWeapon(victim);
        if (!"shield".equalsIgnoreCase(assigned)) {
            return;
        }

        // If the player successfully blocks the incoming damage
        if (victim.isBlocking() && event.getFinalDamage() < event.getDamage()) {
            final ShieldSettings settings = plugin.getConfigHandler().getConfig().weapons.shield;
            final int currentCharge = ultimateHits.getOrDefault(uuid, 0);
            final int targetCharge = settings.ultimateHitsRequired;

            if (currentCharge < targetCharge) {
                final int nextCharge = currentCharge + 1;
                ultimateHits.put(uuid, nextCharge);

                if (nextCharge == targetCharge) {
                    victim.sendMessage(ColorParser.of("<green><bold>Shield Ultimate is fully charged! Use /ability to activate!</bold></green>").build());
                    victim.playSound(victim.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                } else {
                    victim.sendMessage(ColorParser.of("<gray>Ultimate Charge: <gold>" + nextCharge + "/" + targetCharge + "</gold> blocks</gray>").build());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        ultimateHits.remove(uuid);
        shieldUltimateActive.remove(uuid);
    }
}
