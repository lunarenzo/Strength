package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
 * Listener handling Mace weapon passive (Seismic Heavy Shockwave) and ultimate charging on hits.
 */
public final class MaceAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final Map<UUID, Integer> passiveHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> activeUltimatePlayers = new ConcurrentHashMap<>();

    public MaceAbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();
        if (!settings.enabled) return;

        final String assignedWeapon = strengthService.getAssignedWeapon(attacker);
        if (!"mace".equalsIgnoreCase(assignedWeapon)) return;

        if (attacker.getInventory().getItemInMainHand().getType() != Material.MACE) return;

        final UUID uuid = attacker.getUniqueId();

        // 1. Mace Passive Logic: Seismic Heavy Shockwave
        if (settings.passive.enabled) {
            final int currentPassiveHits = passiveHitsMap.getOrDefault(uuid, 0) + 1;

            if (currentPassiveHits >= settings.passive.hitsRequired) {
                passiveHitsMap.put(uuid, 0);

                // Multiply damage on passive trigger
                event.setDamage(event.getDamage() * settings.passive.damageMultiplier);

                // Shockwave visual & audio effects
                final Location loc = target.getLocation();
                if (loc.getWorld() != null) {
                    loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 0.5, 0), 3, 0.5, 0.2, 0.5, 0.05);
                    loc.getWorld().spawnParticle(Particle.SWEEP_ATTACK, loc.clone().add(0, 1.0, 0), 5, 0.3, 0.3, 0.3, 0.1);
                    loc.getWorld().playSound(loc, Sound.ITEM_MACE_SMASH_GROUND, 1.2f, 0.8f);

                    // AoE shockwave damage to nearby entities
                    final double radius = settings.passive.shockwaveRadius;
                    for (Entity nearby : loc.getWorld().getNearbyEntities(loc, radius, radius, radius)) {
                        if (nearby instanceof LivingEntity nearbyLiving && !nearbyLiving.equals(attacker) && !nearbyLiving.equals(target)) {
                            nearbyLiving.damage(event.getDamage() * 0.4, attacker);
                        }
                    }
                }

                if (settings.passive.passiveTriggeredMessage != null && !settings.passive.passiveTriggeredMessage.isBlank()) {
                    attacker.sendMessage(ColorParser.of(settings.passive.passiveTriggeredMessage).build());
                }
            } else {
                passiveHitsMap.put(uuid, currentPassiveHits);
            }
        }

        // 2. Mace Ultimate Charge Logic
        if (settings.ultimate.enabled) {
            final int currentHits = ultimateHitsMap.getOrDefault(uuid, 0);

            if (currentHits < settings.ultimate.hitsRequired) {
                final int newHits = currentHits + 1;
                ultimateHitsMap.put(uuid, newHits);

                if (newHits >= settings.ultimate.hitsRequired) {
                    if (settings.ultimate.ultimateChargedMessage != null && !settings.ultimate.ultimateChargedMessage.isBlank()) {
                        attacker.sendMessage(ColorParser.of(settings.ultimate.ultimateChargedMessage).build());
                    }
                } else {
                    if (settings.ultimate.ultimateChargeProgressMessage != null && !settings.ultimate.ultimateChargeProgressMessage.isBlank()) {
                        final String msg = settings.ultimate.ultimateChargeProgressMessage
                            .replace("{charge}", String.valueOf(newHits))
                            .replace("{target}", String.valueOf(settings.ultimate.hitsRequired));
                        attacker.sendMessage(ColorParser.of(msg).build());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        passiveHitsMap.remove(uuid);
        ultimateHitsMap.remove(uuid);
        ultimateCooldowns.remove(uuid);
        activeUltimatePlayers.remove(uuid);
    }
}
