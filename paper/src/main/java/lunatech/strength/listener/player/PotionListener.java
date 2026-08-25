package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.PotionConfig;
import lunatech.strength.config.PluginConfig.MessagesConfig;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.Material;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BrewingStartEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Listener handling Potion, Potion Effect, and Brewing restrictions.
 */
public final class PotionListener implements Listener {
    private final Strength plugin;

    public PotionListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    private PotionConfig getConfig() {
        return plugin.getConfigHandler().getPotionConfig();
    }

    private MessagesConfig getMessages() {
        return plugin.getConfigHandler().getConfig().messages;
    }

    /* =========================================================================
     * 1. Entity Potion Effect Application Guard
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPotionEffect(@NotNull EntityPotionEffectEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null) {
            return;
        }

        if (isEffectRestricted(newEffect.getType(), config)) {
            event.setCancelled(true);
            final String effectName = getFriendlyEffectName(newEffect.getType());
            MessageUtil.send(player, getMessages().potionEffectRemovedMessage, "effect", effectName);
        }
    }

    /* =========================================================================
     * 2. Player Potion Consumption Guard
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerItemConsume(@NotNull PlayerItemConsumeEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled || !config.blockConsumption) {
            return;
        }

        final ItemStack item = event.getItem();
        if (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION && item.getType() != Material.LINGERING_POTION) {
            return;
        }

        if (item.getItemMeta() instanceof PotionMeta potionMeta) {
            if (isPotionMetaRestricted(potionMeta, config)) {
                event.setCancelled(true);
                MessageUtil.send(event.getPlayer(), getMessages().potionBlockedMessage);
            }
        }
    }

    /* =========================================================================
     * 3. Splash & Lingering Potion Splash Guards
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPotionSplash(@NotNull PotionSplashEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final ThrownPotion potion = event.getPotion();
        if (potion.getItem().getItemMeta() instanceof PotionMeta potionMeta) {
            if (isPotionMetaRestricted(potionMeta, config)) {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player player) {
                        event.setIntensity(player, 0.0);
                        MessageUtil.send(player, getMessages().potionBlockedMessage);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLingeringPotionSplash(@NotNull LingeringPotionSplashEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled) {
            return;
        }

        final ThrownPotion potion = event.getEntity();
        if (potion.getItem().getItemMeta() instanceof PotionMeta potionMeta) {
            if (isPotionMetaRestricted(potionMeta, config)) {
                event.setCancelled(true);
            }
        }
    }

    /* =========================================================================
     * 4. Brewing Restrictions (BrewingStartEvent & BrewEvent)
     * ========================================================================= */

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBrewingStart(@NotNull BrewingStartEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled || !config.blockBrewing) {
            return;
        }

        if (event.getBlock().getState() instanceof BrewingStand stand) {
            final var inv = stand.getInventory();
            final ItemStack ingredient = inv.getIngredient();
            if (ingredient == null || ingredient.getType() == Material.AIR) {
                return;
            }
            for (int i = 0; i < 3; i++) {
                final ItemStack potion = inv.getItem(i);
                if (isBrewingRestricted(ingredient, potion, config)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBrew(@NotNull BrewEvent event) {
        final PotionConfig config = getConfig();
        if (!config.enabled || !config.blockBrewing) {
            return;
        }

        final var inv = event.getContents();
        final ItemStack ingredient = inv.getIngredient();
        if (ingredient == null || ingredient.getType() == Material.AIR) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            final ItemStack potion = inv.getItem(i);
            if (isBrewingRestricted(ingredient, potion, config)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /* =========================================================================
     * Utility Helpers
     * ========================================================================= */

    private boolean isBrewingRestricted(@NotNull ItemStack ingredient, @Nullable ItemStack potion, @NotNull PotionConfig config) {
        if (potion == null || potion.getType() == Material.AIR) {
            return false;
        }

        if (potion.getItemMeta() instanceof PotionMeta potionMeta) {
            if (isPotionMetaRestricted(potionMeta, config)) {
                return true;
            }

            final Material ingType = ingredient.getType();
            final String baseType = getBasePotionTypeName(potionMeta);

            // Blaze Powder -> Strength
            if (ingType == Material.BLAZE_POWDER && isEffectRestricted(PotionEffectType.STRENGTH, config)) {
                if (baseType.contains("AWKWARD") || baseType.contains("WATER") || baseType.contains("UNCRAFTABLE")) {
                    return true;
                }
            }

            // Fermented Spider Eye conversions
            if (ingType == Material.FERMENTED_SPIDER_EYE) {
                if (baseType.contains("NIGHT_VISION") && isEffectRestricted(PotionEffectType.INVISIBILITY, config)) {
                    return true;
                }
                if ((baseType.contains("SWIFTNESS") || baseType.contains("SPEED") || baseType.contains("LEAPING"))
                        && isEffectRestricted(PotionEffectType.SLOWNESS, config)) {
                    return true;
                }
                if ((baseType.contains("HEALING") || baseType.contains("POISON"))
                        && isEffectRestricted(PotionEffectType.INSTANT_DAMAGE, config)) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getBasePotionTypeName(@NotNull PotionMeta meta) {
        try {
            if (meta.getBasePotionType() != null) {
                return meta.getBasePotionType().name().toUpperCase();
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    private boolean isPotionMetaRestricted(@NotNull PotionMeta meta, @NotNull PotionConfig config) {
        for (PotionEffect customEffect : meta.getCustomEffects()) {
            if (isEffectRestricted(customEffect.getType(), config)) {
                return true;
            }
        }
        try {
            if (meta.getBasePotionType() != null) {
                for (PotionEffect baseEffect : meta.getBasePotionType().getPotionEffects()) {
                    if (isEffectRestricted(baseEffect.getType(), config)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
            // Graceful fallback across Paper version API differences
        }
        return false;
    }

    private boolean isEffectRestricted(@NotNull PotionEffectType type, @NotNull PotionConfig config) {
        final String key = type.getKey().toString().toLowerCase();
        final String name = type.getKey().getKey().toLowerCase();

        boolean matched = false;
        for (String entry : config.blacklistedEffects) {
            final String lower = entry.trim().toLowerCase();
            if (key.equals(lower) || name.equals(lower)
                    || (lower.contains("strength") && (name.contains("strength") || name.contains("increase_damage")))) {
                matched = true;
                break;
            }
        }

        final boolean isBlacklist = "BLACKLIST".equalsIgnoreCase(config.mode);
        return isBlacklist ? matched : !matched;
    }

    private String getFriendlyEffectName(@NotNull PotionEffectType type) {
        final String keyName = type.getKey().getKey().replace('_', ' ');
        return Character.toUpperCase(keyName.charAt(0)) + keyName.substring(1);
    }
}
