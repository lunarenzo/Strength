package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.SwordConfig;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.SwordUltimateTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.util.Vector;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.RayTraceResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Sword abilities: tracking consecutive melee hits for Auto-Crit passive,
 * Ultimate charge accumulation, offhand Dual Wielding attack execution, 50% attack cooldown attribute scaling,
 * and strict anti-duplication inventory guardrails.
 */
public final class SwordAbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    // Collections to manage active weapon state and eliminate memory leaks
    public static final Map<UUID, Integer> comboCounts = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastHitTimes = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ultimateHits = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Boolean> activeDualWield = new ConcurrentHashMap<>();
    public static final Map<UUID, ItemStack> originalOffhandItems = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> lastOffhandAttackTimes = new ConcurrentHashMap<>();

    private static NamespacedKey cloneKey;

    public SwordAbilityListener(Strength plugin, StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
        cloneKey = new NamespacedKey(plugin, "dual_wield_clone");
    }

    public static NamespacedKey getCloneKey() {
        return cloneKey;
    }

    public static boolean isClone(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(cloneKey, PersistentDataType.BYTE);
    }

    public static void markAsClone(ItemStack item) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(cloneKey, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
    }

    public static void endDualWield(Player player, Strength plugin) {
        final UUID uuid = player.getUniqueId();
        if (activeDualWield.remove(uuid) == null) {
            return;
        }

        // 1. Remove 100% Attack Speed Attribute Modifier (+50% cooldown reduction)
        final AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            attr.removeModifier(new NamespacedKey(plugin, "sword_ult_speed"));
        }

        // 2. Clear cloned offhand sword
        final ItemStack offhand = player.getInventory().getItemInOffHand();
        if (isClone(offhand)) {
            player.getInventory().setItemInOffHand(null);
        }

        // 3. Restore original saved offhand item if present
        final ItemStack saved = originalOffhandItems.remove(uuid);
        if (saved != null) {
            player.getInventory().setItemInOffHand(saved);
        }

        lastOffhandAttackTimes.remove(uuid);

        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
        player.sendMessage(ColorParser.of(settings.ultimateExpiredMessage).build());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwordHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim) || !(event.getDamager() instanceof Player damager)) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }

        // Ignore offhand strikes (offhand combo counter is already processed in executeOffhandDamage)
        if (damager.hasMetadata("sword_offhand_strike")) {
            return;
        }

        // Ignore uncharged spam strikes (must be >= 0.9f full attack strength, matching 1:1 main-hand vanilla)
        if (damager.getCooledAttackStrength(0.0f) < 0.9f) {
            return;
        }

        if (!Tag.ITEMS_SWORDS.isTagged(damager.getInventory().getItemInMainHand().getType())) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(damager);
        if (!"sword".equalsIgnoreCase(assigned)) {
            return;
        }

        // WorldGuard region check for weapon ability
        if (!lunatech.strength.integration.WorldGuardHook.isAbilityAllowed(plugin, damager, victim.getLocation())) {
            return;
        }

        final double modifiedDmg = processSwordHitCombo(damager, victim, event.getDamage());
        event.setDamage(modifiedDmg);
    }

    private double processSwordHitCombo(Player damager, LivingEntity victim, double baseDamage) {
        final UUID uuid = damager.getUniqueId();
        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();

        final long now = System.currentTimeMillis();
        final long timeoutMs = (long) (settings.passiveComboTimeoutSeconds * 1000.0);
        final long lastHit = lastHitTimes.getOrDefault(uuid, 0L);

        int combo = (now - lastHit > timeoutMs) ? 0 : comboCounts.getOrDefault(uuid, 0);
        combo++;
        lastHitTimes.put(uuid, now);

        double finalDamage = baseDamage;

        if (combo >= settings.passiveComboHitsRequired) {
            comboCounts.put(uuid, 0);
            finalDamage = baseDamage * settings.passiveCritDamageMultiplier;

            victim.getWorld().spawnParticle(Particle.CRIT, victim.getLocation().add(0, 1.0, 0), 15, 0.3, 0.5, 0.3, 0.1);
            victim.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, victim.getLocation().add(0, 1.0, 0), 5, 0.2, 0.4, 0.2, 0.1);
            damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
            damager.sendMessage(ColorParser.of(settings.passiveAutoCritMessage).build());

            // Increment Ultimate Charge on Passive Trigger (only when NOT in active dual wield)
            if (!activeDualWield.getOrDefault(uuid, false)) {
                final int currentUltHits = ultimateHits.getOrDefault(uuid, 0);
                final int targetUltHits = settings.ultimateHitsRequired;
                if (currentUltHits < targetUltHits) {
                    final int nextUltHits = currentUltHits + 1;
                    ultimateHits.put(uuid, nextUltHits);

                    if (nextUltHits == targetUltHits) {
                        damager.sendMessage(ColorParser.of(settings.ultimateChargedMessage).build());
                        damager.playSound(damager.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.2f);
                    } else {
                        damager.sendMessage(
                            ColorParser.of(settings.ultimateChargeProgressMessage
                                .replace("{charge}", String.valueOf(nextUltHits))
                                .replace("{target}", String.valueOf(targetUltHits)))
                                .with("charge", String.valueOf(nextUltHits))
                                .with("target", String.valueOf(targetUltHits))
                                .build()
                        );
                    }
                }
            }
        } else {
            comboCounts.put(uuid, combo);
            damager.sendMessage(
                ColorParser.of(settings.passiveComboProgressMessage
                    .replace("{combo}", String.valueOf(combo))
                    .replace("{required}", String.valueOf(settings.passiveComboHitsRequired)))
                    .with("combo", String.valueOf(combo))
                    .with("required", String.valueOf(settings.passiveComboHitsRequired))
                    .build()
            );
        }

        return finalDamage;
    }

    // Offhand Attack: Right-Click Entity
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onOffhandInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (event.getHand() == EquipmentSlot.OFF_HAND && activeDualWield.getOrDefault(uuid, false)) {
            event.setCancelled(true);
            if (event.getRightClicked() instanceof LivingEntity target) {
                executeOffhandDamage(player, target);
            }
        }
    }

    // Offhand Attack: Right-Click Air / Block
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOffhandInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        if (event.getHand() == EquipmentSlot.OFF_HAND && activeDualWield.getOrDefault(uuid, false)) {
            final Action action = event.getAction();
            if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.swingOffHand();

                final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
                final RayTraceResult result = player.getWorld().rayTraceEntities(
                    player.getEyeLocation(),
                    player.getEyeLocation().getDirection(),
                    settings.offhandReachDistance,
                    e -> e instanceof LivingEntity && !e.equals(player)
                );

                if (result != null && result.getHitEntity() instanceof LivingEntity target) {
                    executeOffhandDamage(player, target);
                }
            }
        }
    }

    private void executeOffhandDamage(Player player, LivingEntity target) {
        // 1. Check attack cooldown indicator (must be >= 0.9f full charge to strike, matching 1:1 main-hand vanilla)
        if (player.getAttackCooldown() < 0.9f) {
            return;
        }

        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final AttributeInstance attackSpeedAttr = player.getAttribute(Attribute.ATTACK_SPEED);
        final double attackSpeed = (attackSpeedAttr != null) ? attackSpeedAttr.getValue() : 4.0;
        final long minIntervalMs = (long) (1000.0 / Math.max(1.0, attackSpeed));
        final long last = lastOffhandAttackTimes.getOrDefault(uuid, 0L);

        // 2. Debounce based on player attack speed attribute to match vanilla main-hand swing rhythm
        if (now - last < minIntervalMs) {
            return;
        }

        lastOffhandAttackTimes.put(uuid, now);

        // Reset attack cooldown bar after offhand strike
        player.resetCooldown();

        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
        final ItemStack offhand = player.getInventory().getItemInOffHand();
        final AttributeInstance damageAttr = player.getAttribute(Attribute.ATTACK_DAMAGE);
        double dmg = damageAttr != null ? damageAttr.getValue() : 6.0;

        // Sharpness enchantment damage bonus
        if (offhand != null && offhand.hasItemMeta() && offhand.getItemMeta().hasEnchant(org.bukkit.enchantments.Enchantment.SHARPNESS)) {
            final int sharpLvl = offhand.getItemMeta().getEnchantLevel(org.bukkit.enchantments.Enchantment.SHARPNESS);
            dmg += (0.5 * sharpLvl + 0.5);
        }

        // Apply passive combo tracking to offhand hits
        dmg = processSwordHitCombo(player, target, dmg);

        // Native jump-crit condition: falling, not climbing, not in water, no blindness, not riding
        final boolean isCrit = player.getFallDistance() > 0.0F 
            && !player.isClimbing() 
            && !player.isInWater() 
            && !player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS) 
            && player.getVehicle() == null;

        if (isCrit) {
            dmg *= 1.5;
            target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1.0, 0), 15, 0.3, 0.5, 0.3, 0.1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 1.0f);
        } else if (!player.isSprinting()) {
            // Native vanilla sweep particle location (1.2 blocks in front of player at chest level)
            final Vector dir = player.getLocation().getDirection().setY(0).normalize();
            final Location sweepLoc = player.getLocation().add(0, 1.0, 0).add(dir.multiply(1.2));
            target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, sweepLoc, 1, 0.0, 0.0, 0.0, 0.0);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

            // Collateral AoE sweep damage to nearby entities
            final double sweepDmg = settings.offhandSweepDamageMultiplier + (offhand != null && offhand.containsEnchantment(org.bukkit.enchantments.Enchantment.SWEEPING_EDGE) 
                ? offhand.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.SWEEPING_EDGE) : 0.0);

            for (org.bukkit.entity.Entity nearby : target.getWorld().getNearbyEntities(target.getBoundingBox().expand(1.0, 0.25, 1.0))) {
                if (nearby instanceof LivingEntity living && !nearby.equals(player) && !nearby.equals(target)) {
                    living.damage(sweepDmg, player);
                }
            }
        }

        // Enchanted hit particle effect
        if (offhand != null && offhand.hasItemMeta() && offhand.getItemMeta().hasEnchants()) {
            target.getWorld().spawnParticle(Particle.ENCHANTED_HIT, target.getLocation().add(0, 1.0, 0), 15, 0.3, 0.5, 0.3, 0.1);
        }

        final org.bukkit.plugin.Plugin plugin = org.bukkit.plugin.java.JavaPlugin.getProvidingPlugin(getClass());
        player.setMetadata("sword_offhand_strike", new org.bukkit.metadata.FixedMetadataValue(plugin, true));
        try {
            target.damage(dmg, player);
        } finally {
            player.removeMetadata("sword_offhand_strike", plugin);
        }
        player.swingOffHand();
    }

    // Anti-Duplication Guardrails
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (isClone(event.getCurrentItem()) || isClone(event.getCursor()) || activeDualWield.containsKey(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isClone(event.getItemDrop().getItemStack()) || activeDualWield.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (activeDualWield.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final UUID uuid = player.getUniqueId();

        // Restore & drop original offhand item on death
        final ItemStack saved = originalOffhandItems.remove(uuid);
        if (saved != null && saved.getType() != org.bukkit.Material.AIR) {
            event.getDrops().add(saved);
        }

        event.getDrops().removeIf(SwordAbilityListener::isClone);
        endDualWield(player, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        comboCounts.remove(uuid);
        lastHitTimes.remove(uuid);
        ultimateHits.remove(uuid);
        ultimateCooldowns.remove(uuid);
        endDualWield(player, plugin);
    }
}
