package lunatech.strength.listener.player;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import lunatech.strength.Strength;
import lunatech.strength.config.Crossbow2Config;
import lunatech.strength.constant.PDCKeys;
import lunatech.strength.service.StrengthService;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener handling Crossbow2 passive abilities (Shield Piercing &amp; Chance-based Flame burn)
 * and exploit-proof temporary enchantment management during Ultimate.
 */
public final class Crossbow2AbilityListener implements Listener {
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> activeUltimatePlayers = new ConcurrentHashMap<>();

    private final Strength plugin;
    private final StrengthService strengthService;

    public Crossbow2AbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    public static boolean isCrossbow(@Nullable ItemStack item) {
        return item != null && item.getType() == Material.CROSSBOW;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final Crossbow2Config config = plugin.getConfigHandler().getCrossbow2Config();
        if (config == null || !config.enabled || !config.passive.enabled) {
            return;
        }

        final String assigned = strengthService.getAssignedWeapon(player);
        if (assigned == null || !"crossbow2".equalsIgnoreCase(assigned)) {
            return;
        }

        final ItemStack bow = event.getBow();
        if (!isCrossbow(bow)) {
            return;
        }

        // WorldGuard region check for weapon ability
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!lunatech.strength.integration.WorldGuardHook.isAbilityAllowed(plugin, player, player.getLocation())) {
                MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.cannotUseAbilityInRegionMessage);
                return;
            }
        }

        final Entity projectile = event.getProjectile();
        if (projectile instanceof AbstractArrow arrow) {
            if (config.passive.shieldPiercing) {
                arrow.setPierceLevel(127);
            }
            final PersistentDataContainer pdc = arrow.getPersistentDataContainer();
            pdc.set(PDCKeys.CROSSBOW2_SHOT, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHitDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Projectile projectile) || !(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        final PersistentDataContainer pdc = projectile.getPersistentDataContainer();
        if (!pdc.has(PDCKeys.CROSSBOW2_SHOT, PersistentDataType.BYTE)) {
            return;
        }

        final Crossbow2Config config = plugin.getConfigHandler().getCrossbow2Config();
        if (config == null || !config.enabled || !config.passive.enabled) {
            return;
        }

        if (projectile.getShooter() instanceof Player shooter) {
            // 1. Shield Piercing Enforcement
            if (config.passive.shieldPiercing && target instanceof Player targetPlayer) {
                if (targetPlayer.isBlocking()) {
                    try {
                        if (event.isApplicable(EntityDamageByEntityEvent.DamageModifier.BLOCKING)) {
                            event.setDamage(EntityDamageByEntityEvent.DamageModifier.BLOCKING, 0.0);
                        }
                    } catch (Throwable ignored) {}
                }
            }

            // 2. Chance-based Flame/Burn Effect
            if (config.passive.flameChance > 0.0) {
                final double roll = ThreadLocalRandom.current().nextDouble(100.0);
                if (roll < strengthService.scale(config.passive.flameChance)) {
                    target.setFireTicks(config.passive.burnDurationSeconds * 20);

                    if (target.getWorld() != null) {
                        target.getWorld().spawnParticle(Particle.FLAME, target.getLocation().add(0, 1.0, 0), 8, 0.2, 0.4, 0.2, 0.05);
                    }

                    if (config.passive.passiveTriggeredMessage != null && !config.passive.passiveTriggeredMessage.isBlank()) {
                        shooter.sendActionBar(ColorParser.of(config.passive.passiveTriggeredMessage).build());
                    }
                }
            }
        }
    }

    /* =========================================================================
     * Auto-Enchantment Management
     * ========================================================================= */

    public static void applyTemporaryEnchantments(@NotNull Player player, @NotNull Crossbow2Config.UltimateConfig settings) {
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (!isCrossbow(mainHand)) return;

        final ItemMeta meta = mainHand.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Capture original enchantment levels before applying temporary ones
        if (!pdc.has(PDCKeys.CROSSBOW2_TEMP_ULT, PersistentDataType.BYTE)) {
            pdc.set(PDCKeys.CROSSBOW2_TEMP_ULT, PersistentDataType.BYTE, (byte) 1);

            final StringBuilder preEnchantsSb = new StringBuilder();
            for (String entry : settings.autoEnchantments) {
                if (entry == null || entry.isBlank()) continue;
                final String[] parts = entry.split(":");
                final String enchKey = parts[0].trim().toLowerCase();
                final NamespacedKey key = NamespacedKey.minecraft(enchKey);
                final Enchantment enchantment = Registry.ENCHANTMENT.get(key);
                if (enchantment != null) {
                    final int preLevel = meta.getEnchantLevel(enchantment);
                    if (preEnchantsSb.length() > 0) {
                        preEnchantsSb.append(";");
                    }
                    preEnchantsSb.append(enchKey).append("=").append(preLevel);
                }
            }
            pdc.set(PDCKeys.CROSSBOW2_PRE_ULT_ENCHANTS, PersistentDataType.STRING, preEnchantsSb.toString());
        }

        for (String entry : settings.autoEnchantments) {
            if (entry == null || entry.isBlank()) continue;
            final String[] parts = entry.split(":");
            if (parts.length < 2) continue;

            final String enchKey = parts[0].trim().toLowerCase();
            final int level;
            try {
                level = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException ignored) {
                continue;
            }

            final NamespacedKey key = NamespacedKey.minecraft(enchKey);
            final Enchantment enchantment = Registry.ENCHANTMENT.get(key);
            if (enchantment != null) {
                meta.addEnchant(enchantment, level, true);
            }
        }

        mainHand.setItemMeta(meta);
    }

    public static void stripTemporaryEnchantments(@Nullable ItemStack item, @NotNull Crossbow2Config.UltimateConfig settings) {
        if (!isCrossbow(item)) return;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        final PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(PDCKeys.CROSSBOW2_TEMP_ULT, PersistentDataType.BYTE)) return;

        final String preEnchantsData = pdc.get(PDCKeys.CROSSBOW2_PRE_ULT_ENCHANTS, PersistentDataType.STRING);
        pdc.remove(PDCKeys.CROSSBOW2_TEMP_ULT);
        pdc.remove(PDCKeys.CROSSBOW2_PRE_ULT_ENCHANTS);

        final Map<String, Integer> preEnchantMap = new HashMap<>();
        if (preEnchantsData != null && !preEnchantsData.isBlank()) {
            final String[] pairs = preEnchantsData.split(";");
            for (String pair : pairs) {
                final String[] kv = pair.split("=");
                if (kv.length == 2) {
                    try {
                        preEnchantMap.put(kv[0].toLowerCase(), Integer.parseInt(kv[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        for (String entry : settings.autoEnchantments) {
            if (entry == null || entry.isBlank()) continue;
            final String[] parts = entry.split(":");
            final String enchKey = parts[0].trim().toLowerCase();

            final NamespacedKey key = NamespacedKey.minecraft(enchKey);
            final Enchantment enchantment = Registry.ENCHANTMENT.get(key);
            if (enchantment != null) {
                final int preLevel = preEnchantMap.getOrDefault(enchKey, 0);
                if (preLevel > 0) {
                    meta.addEnchant(enchantment, preLevel, true);
                } else {
                    meta.removeEnchant(enchantment);
                }
            }
        }

        item.setItemMeta(meta);
    }

    public static void stripTemporaryEnchantmentsFromPlayer(@NotNull Player player, @NotNull Crossbow2Config.UltimateConfig settings) {
        for (ItemStack item : player.getInventory().getContents()) {
            stripTemporaryEnchantments(item, settings);
        }
    }

    /* =========================================================================
     * Event Guards for Exploit Prevention &amp; Cleanup
     * ========================================================================= */

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        final Item droppedItem = event.getItemDrop();
        final Crossbow2Config settings = plugin.getConfigHandler().getCrossbow2Config();
        if (settings != null) {
            stripTemporaryEnchantments(droppedItem.getItemStack(), settings.ultimate);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        final Crossbow2Config settings = plugin.getConfigHandler().getCrossbow2Config();
        if (settings != null) {
            stripTemporaryEnchantments(event.getCurrentItem(), settings.ultimate);
            stripTemporaryEnchantments(event.getCursor(), settings.ultimate);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final UUID uuid = event.getEntity().getUniqueId();
        activeUltimatePlayers.remove(uuid);

        final Crossbow2Config settings = plugin.getConfigHandler().getCrossbow2Config();
        if (settings != null) {
            for (ItemStack drop : event.getDrops()) {
                stripTemporaryEnchantments(drop, settings.ultimate);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        activeUltimatePlayers.remove(uuid);
        ultimateCooldowns.remove(uuid);

        final Crossbow2Config settings = plugin.getConfigHandler().getCrossbow2Config();
        if (settings != null) {
            stripTemporaryEnchantmentsFromPlayer(player, settings.ultimate);
        }
    }
}
