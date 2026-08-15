package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.SwordConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Sword abilities: tracking consecutive melee hits on players with
 * strict guardrails against sweep attacks, weak attack spamming, and time interval timeouts.
 */
public final class SwordAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Collections to manage active weapon state and eliminate memory leaks
    public static final Map<UUID, Integer> comboCounts = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastHitTimes = new ConcurrentHashMap<>();

    public SwordAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwordHit(@NotNull EntityDamageByEntityEvent event) {
        // Target: Players ONLY
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }

        // Damager: Player ONLY
        if (!(event.getDamager() instanceof Player damager)) {
            return;
        }

        // Edge Case 1: Ignore sweep attack damage (only primary melee hits count)
        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }

        // Edge Case 3: Only count hit if attack cooldown tracker is fully charged (>= 0.9f)
        if (damager.getAttackCooldown() < 0.9f) {
            return;
        }

        // Weapon check: Damager must be holding a sword
        if (!Tag.ITEMS_SWORDS.isTagged(damager.getInventory().getItemInMainHand().getType())) {
            return;
        }

        // Verify damager has Sword weapon assigned
        final String assigned = strengthService.getAssignedWeapon(damager);
        if (!"sword".equalsIgnoreCase(assigned)) {
            return;
        }

        final UUID uuid = damager.getUniqueId();
        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();

        final long now = System.currentTimeMillis();
        final long timeoutMs = (long) (settings.passiveComboTimeoutSeconds * 1000.0);
        final long lastHit = lastHitTimes.getOrDefault(uuid, 0L);

        // Edge Case 2: Reset combo if time since last successful hit exceeds timeout interval
        int combo = (now - lastHit > timeoutMs) ? 0 : comboCounts.getOrDefault(uuid, 0);
        combo++;
        
        lastHitTimes.put(uuid, now);

        if (combo >= settings.passiveComboHitsRequired) {
            // Trigger Auto-Crit passive
            comboCounts.put(uuid, 0); // reset combo count

            event.setDamage(event.getDamage() * settings.passiveCritDamageMultiplier);

            // Edge Case 4: Crit visual & sound effects broadcast
            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.0, 0), 15, 0.3, 0.5, 0.3, 0.1);
            victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0, 1.0, 0), 5, 0.2, 0.4, 0.2, 0.1);
            damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);

            damager.sendMessage(ColorParser.of(settings.passiveAutoCritMessage).build());
        } else {
            comboCounts.put(uuid, combo);
            damager.sendMessage(
                ColorParser.of(settings.passiveComboProgressMessage)
                    .with("combo", String.valueOf(combo))
                    .with("required", String.valueOf(settings.passiveComboHitsRequired))
                    .build()
            );
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        comboCounts.remove(uuid);
        lastHitTimes.remove(uuid);
    }
}
