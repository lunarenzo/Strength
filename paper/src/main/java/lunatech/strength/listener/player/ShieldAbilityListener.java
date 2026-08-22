package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.ShieldConfig;
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
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Shield abilities: tracking shield blocks to charge the ultimate,
 * applying configurable percentage passive damage reduction when the shield is disabled or broken,
 * and managing god-mode damage negation and knockback when the ultimate is active.
 */
public final class ShieldAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Concurrent collections to ensure zero memory retention and thread safety
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> shieldUltimateActive = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> passiveProtectionExpiry = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();

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
            event.setDamage(0.0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onEntityDamageHighest(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        if (shieldUltimateActive.getOrDefault(victim.getUniqueId(), false)) {
            event.setCancelled(true);
            event.setDamage(0.0);
        }
    }

    // Edge Case 1: Trigger passive protection when an axe disables player's shield
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShieldDisable(@NotNull PlayerShieldDisableEvent event) {
        triggerPassiveProtection(event.getPlayer());
    }

    // Edge Case 2: Backup trigger when shield durability reaches 0 and item breaks
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        if (event.getBrokenItem().getType() == Material.SHIELD) {
            triggerPassiveProtection(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamagePassive(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        final UUID uuid = victim.getUniqueId();
        final String assigned = strengthService.getAssignedWeapon(victim);
        if (!"shield".equalsIgnoreCase(assigned)) {
            return;
        }

        // WorldGuard region check for weapon ability
        if (!lunatech.strength.integration.WorldGuardHook.isAbilityAllowed(plugin, victim, victim.getLocation())) {
            return;
        }

        final long now = System.currentTimeMillis();
        final long expiry = passiveProtectionExpiry.getOrDefault(uuid, 0L);

        // Passive Ability: Configurable damage reduction when shield is disabled/broken or on cooldown
        if (now < expiry || victim.hasCooldown(Material.SHIELD)) {
            final ShieldConfig settings = plugin.getConfigHandler().getShieldConfig();
            final double multiplier = Math.max(0.0, 1.0 - (settings.passiveDamageReductionPercentage / 100.0));
            event.setDamage(event.getDamage() * multiplier);
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
            event.setDamage(0.0);

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
            final ShieldConfig settings = plugin.getConfigHandler().getShieldConfig();
            final int currentCharge = ultimateHits.getOrDefault(uuid, 0);
            final int targetCharge = settings.ultimateHitsRequired;

            if (currentCharge < targetCharge) {
                final int nextCharge = currentCharge + 1;
                ultimateHits.put(uuid, nextCharge);

                if (nextCharge == targetCharge) {
                    victim.sendMessage(ColorParser.of(settings.ultimateChargedMessage).build());
                    victim.playSound(victim.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                } else {
                    victim.sendMessage(
                        ColorParser.of(settings.ultimateChargeProgressMessage
                            .replace("{charge}", String.valueOf(nextCharge))
                            .replace("{target}", String.valueOf(targetCharge)))
                            .with("charge", String.valueOf(nextCharge))
                            .with("target", String.valueOf(targetCharge))
                            .build()
                    );
                }
            }
        }
    }

    private void triggerPassiveProtection(@NotNull Player player) {
        final String assigned = strengthService.getAssignedWeapon(player);
        if (!"shield".equalsIgnoreCase(assigned)) {
            return;
        }

        final ShieldConfig settings = plugin.getConfigHandler().getShieldConfig();
        final long expiry = System.currentTimeMillis() + (settings.passiveProtectionDurationSeconds * 1000L);
        passiveProtectionExpiry.put(player.getUniqueId(), expiry);

        player.sendMessage(
            ColorParser.of(settings.passiveActivatedMessage
                .replace("{seconds}", String.valueOf(settings.passiveProtectionDurationSeconds))
                .replace("{reduction}", String.valueOf((int) settings.passiveDamageReductionPercentage)))
                .with("seconds", String.valueOf(settings.passiveProtectionDurationSeconds))
                .with("reduction", String.valueOf((int) settings.passiveDamageReductionPercentage))
                .build()
        );
        player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.8f);
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        ultimateHits.remove(uuid);
        shieldUltimateActive.remove(uuid);
        passiveProtectionExpiry.remove(uuid);
        ultimateCooldowns.remove(uuid);
    }
}
