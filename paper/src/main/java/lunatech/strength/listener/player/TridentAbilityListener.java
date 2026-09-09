package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.TridentConfig;
import lunatech.strength.hook.betterteams.BetterTeamsHook;
import lunatech.strength.integration.WorldGuardHook;
import lunatech.strength.service.StrengthService;
import lunatech.strength.utility.MessageUtil;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Material;
import org.bukkit.Particle;
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
    private static final String BARRAGE_ACTIVE_KEY = "trident_barrage_active";

    private final Strength plugin;
    private final StrengthService strengthService;

    // Thread-safe maps for tracking hits and cooldowns. Cleaned up on PlayerQuitEvent to prevent structural memory leaks.
    public static final Map<UUID, Integer> passiveHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();

    public TridentAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player damagee)) {
            return;
        }

        final TridentConfig settings = plugin.getConfigHandler().getTridentConfig();
        if (settings == null || !settings.enabled) {
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

        // WorldGuard region check for weapon ability
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!WorldGuardHook.isAbilityAllowed(plugin, damager, damagee.getLocation())) {
                MessageUtil.send(damager, plugin.getConfigHandler().getConfig().messages.cannotUseAbilityInRegionMessage);
                return;
            }
        }

        if (!BetterTeamsHook.canDamage(damager, damagee)) {
            return;
        }

        final UUID damagerUuid = damager.getUniqueId();

        // 1. Passive Trigger: Every N hits, summon visual lightning bolt and apply configured passive damage
        if (settings.passive != null && settings.passive.enabled) {
            final int currentPassiveHits = passiveHits.merge(damagerUuid, (int) (1 * plugin.getLicenseManager().getScale()), Integer::sum);
            if (currentPassiveHits >= settings.passive.hitsRequired) {
                passiveHits.put(damagerUuid, 0); // Reset count back to 0 immediately

                // Apply configured damage multiplier + extra bonus damage
                final double baseDamage = event.getDamage();
                final double scale = plugin.getLicenseManager().getScale();
                final double multipliedDamage = (baseDamage * (1.0 + (settings.passive.damageMultiplier - 1.0) * scale)) + (settings.passive.lightningDamage * scale);
                event.setDamage(multipliedDamage);

                // Visual lightning effect (does not deal vanilla 5.0 damage or start fires)
                damagee.getWorld().strikeLightningEffect(damagee.getLocation());

                // Animated 2D Billboard Yellow Lightning Particle
                Particle particleType = Particle.WAX_OFF;
                try {
                    particleType = Particle.valueOf(settings.passive.particleType.toUpperCase());
                } catch (Exception ignored) {
                }

                damagee.getWorld().spawnParticle(
                    particleType,
                    damagee.getLocation().add(0, 1, 0),
                    15, 0.4, 0.8, 0.4, 0.05
                );

                damagee.playSound(damagee.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f, 1.0f);
                damager.sendMessage(ColorParser.of(settings.passive.passiveTriggeredMessage).build());
            }
        }

        // 2. Ultimate Charge: Accumulate N hits to unlock Ultimate (skipped during active barrage to prevent infinite loops)
        if (settings.ultimate != null && settings.ultimate.enabled) {
            if (damager.hasMetadata(BARRAGE_ACTIVE_KEY)) {
                return;
            }

            final int currentUltHits = ultimateHits.getOrDefault(damagerUuid, 0);
            final int targetUltHits = settings.ultimate.hitsRequired;
            if (currentUltHits < targetUltHits) {
                final int nextUltHits = currentUltHits + (int) (1 * plugin.getLicenseManager().getScale());
                ultimateHits.put(damagerUuid, nextUltHits);

                if (nextUltHits == targetUltHits) {
                    damager.sendMessage(ColorParser.of(settings.ultimate.ultimateChargedMessage).build());
                    damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                } else {
                    final String msg = settings.ultimate.ultimateChargeProgressMessage
                        .replace("<charge>", String.valueOf(nextUltHits))
                        .replace("{charge}", String.valueOf(nextUltHits))
                        .replace("<target>", String.valueOf(targetUltHits))
                        .replace("{target}", String.valueOf(targetUltHits));
                    damager.sendMessage(
                        ColorParser.of(msg)
                            .with("charge", String.valueOf(nextUltHits))
                            .with("target", String.valueOf(targetUltHits))
                            .build()
                    );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        // Immediate cleanup of cached collections to guarantee zero heap accumulation over time
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHits.remove(uuid);
        ultimateHits.remove(uuid);
        ultimateCooldowns.remove(uuid);
    }
}
