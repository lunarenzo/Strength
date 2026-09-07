package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.Trident2Config;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener that manages Trident2 weapon abilities:
 * 1. Passive Sword Attack Speed (+0.5 attribute bonus) & Axe Shield Stun on blocking targets.
 * 2. Thunderstorm Ultimate: Lightning strikes on every critical hit during active ultimate.
 */
public final class Trident2AbilityListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public static final Map<UUID, Long> activeUltimatePlayers = new ConcurrentHashMap<>();
    public static final Map<UUID, Long> ultimateCooldowns = new ConcurrentHashMap<>();

    private static NamespacedKey attackSpeedKey;

    public Trident2AbilityListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
        attackSpeedKey = new NamespacedKey(plugin, "trident2_attack_speed");
    }

    public static void endUltimate(@NotNull Player player, @NotNull Strength plugin) {
        final UUID uuid = player.getUniqueId();
        if (activeUltimatePlayers.remove(uuid) != null) {
            final Trident2Config settings = plugin.getConfigHandler().getTrident2Config();
            if (player.isOnline() && settings.ultimate.ultimateExpiredMessage != null && !settings.ultimate.ultimateExpiredMessage.isBlank()) {
                player.sendMessage(ColorParser.of(settings.ultimate.ultimateExpiredMessage).build());
            }
        }
    }

    private void updateAttackSpeedAttribute(@NotNull Player player, boolean holdingTrident2) {
        final AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr == null) return;

        attr.removeModifier(attackSpeedKey);

        if (holdingTrident2) {
            final Trident2Config settings = plugin.getConfigHandler().getTrident2Config();
            if (settings.enabled && settings.passive.enabled && settings.passive.swordAttackSpeedEnabled) {
                final AttributeModifier modifier = new AttributeModifier(
                    attackSpeedKey,
                    settings.passive.attackSpeedBonus,
                    AttributeModifier.Operation.ADD_NUMBER
                );
                attr.addModifier(modifier);
            }
        }
    }

    private boolean isHoldingTrident2(@NotNull Player player) {
        final ItemStack mainhand = player.getInventory().getItemInMainHand();
        if (mainhand == null || mainhand.getType() != Material.TRIDENT) {
            return false;
        }
        final String assigned = strengthService.getAssignedWeapon(player);
        return "trident2".equalsIgnoreCase(assigned);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(@NotNull EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player damagee)) {
            return;
        }

        if (!isHoldingTrident2(damager)) {
            return;
        }

        // WorldGuard region check for weapon ability
        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!lunatech.strength.integration.WorldGuardHook.isAbilityAllowed(plugin, damager, damagee.getLocation())) {
                lunatech.strength.utility.MessageUtil.send(damager, plugin.getConfigHandler().getConfig().messages.cannotUseAbilityInRegionMessage);
                return;
            }
        }

        if (!lunatech.strength.hook.betterteams.BetterTeamsHook.canDamage(damager, damagee)) {
            return;
        }

        final Trident2Config settings = plugin.getConfigHandler().getTrident2Config();
        if (!settings.enabled) {
            return;
        }

        // 1. Passive: Shield Stun (Axe-style shield disable on blocking target)
        if (settings.passive.enabled && settings.passive.shieldStunEnabled && damagee.isBlocking()) {
            final int stunTicks = (int) Math.max(1, settings.passive.shieldStunDurationSeconds * 20);
            damagee.setCooldown(Material.SHIELD, stunTicks);

            damagee.getWorld().playSound(damagee.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 0.9f);
            damagee.getWorld().spawnParticle(
                Particle.ITEM,
                damagee.getLocation().add(0, 1.2, 0),
                10, 0.2, 0.2, 0.2, 0.05,
                new ItemStack(Material.SHIELD)
            );

            if (settings.passive.shieldStunMessage != null && !settings.passive.shieldStunMessage.isBlank()) {
                damager.sendMessage(ColorParser.of(settings.passive.shieldStunMessage
                    .replace("<target>", damagee.getName())
                    .replace("{target}", damagee.getName())).build());
            }

            if (settings.passive.shieldStaggeredMessage != null && !settings.passive.shieldStaggeredMessage.isBlank()) {
                damagee.sendMessage(ColorParser.of(settings.passive.shieldStaggeredMessage).build());
            }
        }

        // 2. Ultimate: Lightning strike on critical hit during active Thunderstorm Ultimate
        final UUID damagerUuid = damager.getUniqueId();
        final Long ultExpiry = activeUltimatePlayers.get(damagerUuid);
        if (ultExpiry != null && System.currentTimeMillis() < ultExpiry && event.isCritical()) {
            // Apply bonus damage
            final double baseDamage = event.getDamage();
            event.setDamage(baseDamage + settings.ultimate.lightningBonusDamage);

            // Lightning strike visual effect
            damagee.getWorld().strikeLightningEffect(damagee.getLocation());

            Particle particleType = Particle.WAX_OFF;
            try {
                particleType = Particle.valueOf(settings.ultimate.lightningParticleType.toUpperCase());
            } catch (Exception ignored) {}

            damagee.getWorld().spawnParticle(
                particleType,
                damagee.getLocation().add(0, 1.0, 0),
                15, 0.4, 0.8, 0.4, 0.05
            );

            damagee.playSound(damagee.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.9f, 1.1f);
        }
    }

    @EventHandler
    public void onItemHeldChange(@NotNull PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final ItemStack nextItem = player.getInventory().getItem(event.getNewSlot());
        final boolean isTrident = nextItem != null && nextItem.getType() == Material.TRIDENT;
        final String assigned = strengthService.getAssignedWeapon(player);
        final boolean holdingTrident2 = isTrident && "trident2".equalsIgnoreCase(assigned);
        updateAttackSpeedAttribute(player, holdingTrident2);
    }

    @EventHandler
    public void onItemDrop(@NotNull PlayerDropItemEvent event) {
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> updateAttackSpeedAttribute(player, isHoldingTrident2(player)));
    }

    @EventHandler
    public void onItemSwap(@NotNull PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTask(plugin, () -> updateAttackSpeedAttribute(player, isHoldingTrident2(player)));
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        activeUltimatePlayers.remove(uuid);
        ultimateCooldowns.remove(uuid);
        updateAttackSpeedAttribute(player, false);
    }

    @EventHandler
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        final Player player = event.getEntity();
        endUltimate(player, plugin);
        updateAttackSpeedAttribute(player, false);
    }
}
