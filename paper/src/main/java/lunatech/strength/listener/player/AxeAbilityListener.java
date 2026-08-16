package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.AxeConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.AxeUltimateTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event listener handling Axe Passive (Seismic Stun) and Ultimate (Executioner's Mark damage storage).
 */
public final class AxeAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final Map<UUID, Integer> criticalHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> stunnedPlayers = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> activeUltimateAttackers = new ConcurrentHashMap<>();
    public static final Map<UUID, Map<UUID, Double>> storedDamagePools = new ConcurrentHashMap<>();

    public AxeAbilityListener(Strength plugin, StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAxeHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player damager)) {
            return;
        }

        final UUID damagerUuid = damager.getUniqueId();
        final UUID victimUuid = victim.getUniqueId();
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

        // 1. Attack Cancellation when Stunned
        final Long stunnedUntil = stunnedPlayers.get(victimUuid);
        if (stunnedUntil != null && System.currentTimeMillis() < stunnedUntil && settings.cancelAttacksWhenStunned) {
            // Damager is currently stunned
            event.setCancelled(true);
            return;
        }

        final ItemStack weapon = damager.getInventory().getItemInMainHand();
        if (weapon == null || !Tag.ITEMS_AXES.isTagged(weapon.getType())) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(damager);
        if (!"axe".equalsIgnoreCase(assigned)) {
            return;
        }

        // Edge Case 1: Check if victim is blocking with a shield
        final boolean isBlocking = victim.isBlocking();
        if (isBlocking && !settings.countShieldHitsAsCrit) {
            return;
        }

        // Native jump-crit condition: falling, not climbing, not in water, no blindness, not riding
        final boolean isCrit = damager.getFallDistance() > 0.0F 
            && !damager.isClimbing() 
            && !damager.isInWater() 
            && !damager.hasPotionEffect(PotionEffectType.BLINDNESS) 
            && damager.getVehicle() == null;

        if (isCrit) {
            // Track ultimate charge
            ultimateHitsMap.merge(damagerUuid, 1, Integer::sum);
            if (ultimateHitsMap.getOrDefault(damagerUuid, 0) == settings.ultimateCritsRequired) {
                damager.sendMessage(ColorParser.of(settings.ultimateChargedMessage).build());
            }

            // Track passive charge
            final int crits = criticalHitsMap.merge(damagerUuid, 1, Integer::sum);

            if (crits >= settings.critsRequired) {
                // Reset passive charge
                criticalHitsMap.put(damagerUuid, 0);

                // Apply Seismic Stun to victim
                final long stunEndTime = System.currentTimeMillis() + (settings.stunDurationSeconds * 1000L);
                stunnedPlayers.put(victimUuid, stunEndTime);

                // Immobilize target (slowness max + jump inhibition)
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, settings.stunDurationSeconds * 20, 255, false, false, true));
                victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, settings.stunDurationSeconds * 20, 128, false, false, true));

                // Particles & Sound
                victim.getWorld().spawnParticle(Particle.ANVIL, victim.getLocation().add(0, 1.0, 0), 20, 0.3, 0.5, 0.3, 0.1);
                victim.playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);

                // Messages
                damager.sendMessage(ColorParser.of(settings.passiveTriggeredAttackerMessage.replace("{seconds}", String.valueOf(settings.stunDurationSeconds))).build());
                victim.sendActionBar(ColorParser.of(settings.stunActionbarMessage.replace("{seconds}", String.valueOf(settings.stunDurationSeconds))).build());
            }
        }

        // 2. Active Ultimate Damage Interception & Storage
        if (activeUltimateAttackers.getOrDefault(damagerUuid, false)) {
            final double finalDamage = event.getFinalDamage();
            storedDamagePools.computeIfAbsent(damagerUuid, k -> new ConcurrentHashMap<>()).merge(victimUuid, finalDamage, Double::sum);
            // Cancel direct damage so damage accumulates for final burst
            event.setDamage(0.0);
        }
    }
}
