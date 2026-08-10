package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.TridentConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that tracks player hits with a trident to trigger the lightning passive and charge the ultimate.
 */
public final class TridentAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Thread-safe maps for tracking hits. Cleaned up on PlayerQuitEvent to prevent structural memory leaks.
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();

    public TridentAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player damagee)) {
            return;
        }

        // Verify that damager is holding a Trident
        if (damager.getInventory().getItemInMainHand().getType() != Material.TRIDENT) {
            return;
        }

        // Verify that damager has the Trident weapon assigned
        final String assignedWeapon = strengthService.getAssignedWeapon(damager);
        if (!"trident".equalsIgnoreCase(assignedWeapon)) {
            return;
        }

        final TridentConfig settings = plugin.getConfigHandler().getTridentConfig();

        // 1. Passive Trigger: Every N hits, summon a lightning bolt that deals extra damage
        final UUID damagerUuid = damager.getUniqueId();
        final int currentPassiveHits = passiveHits.merge(damagerUuid, 1, Integer::sum);
        if (currentPassiveHits >= settings.passiveHitsRequired) {
            passiveHits.put(damagerUuid, 0); // Reset count

            // Visual lightning effect (does not damage terrain or trigger fire/griefing)
            damagee.getWorld().strikeLightningEffect(damagee.getLocation());

            // Deal faked lightning damage
            damagee.damage(settings.passiveLightningDamage, damager);

            damagee.playSound(damagee.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.0f);
            damager.sendMessage(ColorParser.of(settings.passiveTriggeredMessage).build());
        }

        // 2. Ultimate Charge: Accumulate N hits to unlock the Ultimate ability
        final int currentUltHits = ultimateHits.getOrDefault(damagerUuid, 0);
        final int targetUltHits = settings.ultimateHitsRequired;
        if (currentUltHits < targetUltHits) {
            final int nextUltHits = currentUltHits + 1;
            ultimateHits.put(damagerUuid, nextUltHits);

            if (nextUltHits == targetUltHits) {
                damager.sendMessage(ColorParser.of(settings.ultimateChargedMessage).build());
                damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
            } else {
                damager.sendMessage(
                    ColorParser.of(settings.ultimateChargeProgressMessage)
                        .with("charge", String.valueOf(nextUltHits))
                        .with("target", String.valueOf(targetUltHits))
                        .build()
                );
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        // Immediate cleanup of cached collections to guarantee zero heap accumulation over time
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
    }
}
