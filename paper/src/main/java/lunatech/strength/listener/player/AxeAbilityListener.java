package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.AxeConfig;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event listener handling Axe Passive (Seismic Stun) and Ultimate (Executioner's Mark damage storage).
 */
public final class AxeAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final NamespacedKey STUN_MODIFIER_KEY = new NamespacedKey("strength", "seismic_stun_speed");
    public static final Map<UUID, Integer> criticalHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHitsMap = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> stunnedPlayers = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> activeUltimateAttackers = new ConcurrentHashMap<>();
    public static final Map<UUID, Map<UUID, Double>> storedDamagePools = new ConcurrentHashMap<>();

    public AxeAbilityListener(Strength plugin, StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;

        // Continuous Actionbar updater for stunned players & ultimate-marked targets
        new BukkitRunnable() {
            @Override
            public void run() {
                final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

                // 1. Stun Actionbar Refresh & Expiration Cleanup
                if (!stunnedPlayers.isEmpty()) {
                    final long now = System.currentTimeMillis();
                    stunnedPlayers.entrySet().removeIf(entry -> {
                        final long until = entry.getValue();
                        if (now >= until) {
                            final Player p = plugin.getServer().getPlayer(entry.getKey());
                            if (p != null) removeStunAttribute(p);
                            return true;
                        }

                        final Player p = plugin.getServer().getPlayer(entry.getKey());
                        if (p != null && p.isOnline()) {
                            final long remainingSec = Math.max(1, (until - now + 999) / 1000);
                            final String msg = settings.stunActionbarMessage.replace("{seconds}", String.valueOf(remainingSec));
                            p.sendActionBar(ColorParser.of(msg).build());
                        }
                        return false;
                    });
                }

                // 2. Ultimate Pending Damage Actionbar Refresh
                if (!storedDamagePools.isEmpty()) {
                    for (Map<UUID, Double> pool : storedDamagePools.values()) {
                        for (Map.Entry<UUID, Double> entry : pool.entrySet()) {
                            if (entry.getValue() > 0.0) {
                                final Player target = plugin.getServer().getPlayer(entry.getKey());
                                if (target != null && target.isOnline()) {
                                    final double totalPending = entry.getValue() * settings.damageMultiplier;
                                    final String msg = settings.pendingDamageActionbarMessage.replace("{amount}", String.format("%.1f", totalPending));
                                    target.sendActionBar(ColorParser.of(msg).build());
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 5L, 5L);
    }

    public static boolean isStunned(Player player) {
        final UUID uuid = player.getUniqueId();
        final Long until = stunnedPlayers.get(uuid);
        if (until == null) return false;
        if (System.currentTimeMillis() >= until) {
            stunnedPlayers.remove(uuid);
            removeStunAttribute(player);
            return false;
        }
        return true;
    }

    public static void applyStunAttribute(Player player) {
        final AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(STUN_MODIFIER_KEY);
            attr.addModifier(new AttributeModifier(
                STUN_MODIFIER_KEY,
                -1.0,
                AttributeModifier.Operation.ADD_SCALAR
            ));
        }
    }

    public static void removeStunAttribute(Player player) {
        final AttributeInstance attr = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (attr != null) {
            attr.removeModifier(STUN_MODIFIER_KEY);
        }
        player.removePotionEffect(PotionEffectType.SLOWNESS);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeStunAttribute(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        if (isStunned(player)) {
            final Location from = event.getFrom();
            final Location to = event.getTo();
            if (to.getY() > from.getY()) {
                // Ground clamp upward velocity natively to block spacebar jumping without flinging
                player.setVelocity(player.getVelocity().setY(0.0));
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStunnedInteract(PlayerInteractEvent event) {
        if (isStunned(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStunnedInteractEntity(PlayerInteractEntityEvent event) {
        if (isStunned(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStunnedBlockPlace(BlockPlaceEvent event) {
        if (isStunned(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onStunnedBlockBreak(BlockBreakEvent event) {
        if (isStunned(event.getPlayer())) {
            event.setCancelled(true);
        }
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
        if (isStunned(damager) && settings.cancelAttacksWhenStunned) {
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

        // 2. Active Ultimate Damage Interception & Storage (ALL attacks: normal & crit)
        if (activeUltimateAttackers.getOrDefault(damagerUuid, false)) {
            final double finalDamage = event.getFinalDamage();
            final Map<UUID, Double> pool = storedDamagePools.computeIfAbsent(damagerUuid, k -> new ConcurrentHashMap<>());
            final double newTotal = pool.merge(victimUuid, finalDamage, Double::sum);

            // Immediate Actionbar notification to victim
            final double pendingBurst = newTotal * settings.damageMultiplier;
            final String msg = settings.pendingDamageActionbarMessage.replace("{amount}", String.format("%.1f", pendingBurst));
            victim.sendActionBar(ColorParser.of(msg).build());

            // Cancel direct damage so damage accumulates for final burst
            event.setDamage(0.0);
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

            // Do not build passive stun charge if victim is ALREADY stunned
            if (!isStunned(victim)) {
                final int crits = criticalHitsMap.merge(damagerUuid, 1, Integer::sum);

                if (crits >= settings.critsRequired) {
                    // Reset passive charge
                    criticalHitsMap.put(damagerUuid, 0);

                    // Apply Seismic Stun to victim (Server & Client synchronized AttributeModifier & Y velocity clamp)
                    final long stunEndTime = System.currentTimeMillis() + (settings.stunDurationSeconds * 1000L);
                    stunnedPlayers.put(victimUuid, stunEndTime);
                    applyStunAttribute(victim);

                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, settings.stunDurationSeconds * 20, 255, false, false, true));

                    // Particles & Sound
                    victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.0, 0), 20, 0.3, 0.5, 0.3, 0.1);
                    victim.playSound(victim.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);

                    // Messages
                    damager.sendMessage(ColorParser.of(settings.passiveTriggeredAttackerMessage.replace("{seconds}", String.valueOf(settings.stunDurationSeconds))).build());
                    victim.sendActionBar(ColorParser.of(settings.stunActionbarMessage.replace("{seconds}", String.valueOf(settings.stunDurationSeconds))).build());
                }
            }
        }
    }
}
