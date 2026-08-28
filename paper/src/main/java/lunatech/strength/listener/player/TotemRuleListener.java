package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.config.RulesConfig;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance, edge-case hardened listener implementing modular Totem of Undying rules,
 * max inventory limits, usage quotas, and PDC-persisted cooldowns across restarts.
 */
public final class TotemRuleListener implements Listener {
    private static final NamespacedKey TOTEM_POP_COUNT_KEY = new NamespacedKey("strength", "totem_pop_count");
    private static final NamespacedKey TOTEM_COOLDOWN_UNTIL_KEY = new NamespacedKey("strength", "totem_cooldown_until");

    // 1-Tick deduplication buffer to prevent multi-damage double quota reduction in identical tick
    private static final Map<UUID, Long> lastResurrectTickMap = new ConcurrentHashMap<>();

    private final Strength plugin;

    public TotemRuleListener(@NotNull Strength plugin) {
        this.plugin = plugin;
    }

    private RulesConfig.TotemRules getTotemRules() {
        return plugin.getConfigHandler().getRulesConfig().totem;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityResurrect(@NotNull EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        final RulesConfig.TotemRules totemRules = getTotemRules();
        if (!totemRules.enabled) {
            return;
        }

        // BANNED mode completely blocks resurrection
        if ("BANNED".equalsIgnoreCase(totemRules.mode)) {
            event.setCancelled(true);
            scheduleInventoryUpdate(player);
            return;
        }

        final boolean isCombat = lunatech.strength.integration.PvPManagerHook.isTaggedInCombat(player);

        // PvPManager active combat check: prevent totem resurrect in combat if enabled
        if (totemRules.preventInCombat && isCombat) {
            event.setCancelled(true);
            scheduleInventoryUpdate(player);
            player.sendMessage(ColorParser.of(totemRules.totemInCombatMessage).build());
            return;
        }

        final long now = System.currentTimeMillis();
        final long cooldownUntil = getTotemCooldownUntil(player);

        if (now < cooldownUntil) {
            // Cooldown ONLY blocks resurrection if quota-only-in-combat is false OR player is in combat!
            // In PvE / non-combat, resurrection is allowed when quotaOnlyInCombat is enabled.
            if (!totemRules.quotaOnlyInCombat || isCombat) {
                event.setCancelled(true);
                scheduleInventoryUpdate(player);

                final long remainingMillis = cooldownUntil - now;
                player.sendMessage(
                    ColorParser.of(totemRules.totemOnCooldownMessage)
                        .with("time", formatTime(remainingMillis))
                        .build()
                );
                return;
            }
        }

        // 1-Tick Deduplication Check
        final UUID uuid = player.getUniqueId();
        final long currentTick = Bukkit.getCurrentTick();
        final long lastTick = lastResurrectTickMap.getOrDefault(uuid, -1L);
        if (lastTick == currentTick) {
            return;
        }
        lastResurrectTickMap.put(uuid, currentTick);

        // Check if quota should be consumed (only consume if not quotaOnlyInCombat OR player is in combat)
        final boolean shouldConsumeQuota = !totemRules.quotaOnlyInCombat || isCombat;

        if (shouldConsumeQuota) {
            final int pops = getTotemPopCount(player) + 1;

            if (totemRules.popQuota > 0 && pops >= totemRules.popQuota) {
                final long durationMillis = totemRules.cooldownDuration.toMillis();
                final long expireTime = now + durationMillis;
                setTotemCooldownUntil(player, expireTime);
                setTotemPopCount(player, 0);

                player.sendMessage(
                    ColorParser.of(totemRules.quotaExhaustedMessage)
                        .with("time", formatTime(durationMillis))
                        .build()
                );
            } else {
                setTotemPopCount(player, pops);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerSwapHandItems(@NotNull PlayerSwapHandItemsEvent event) {
        final Player player = event.getPlayer();
        final RulesConfig.TotemRules totemRules = getTotemRules();
        if (!totemRules.enabled) {
            return;
        }

        final ItemStack main = event.getMainHandItem();
        final ItemStack off = event.getOffHandItem();

        final boolean involvesTotem = (main != null && main.getType() == Material.TOTEM_OF_UNDYING)
            || (off != null && off.getType() == Material.TOTEM_OF_UNDYING);

        if (!involvesTotem) {
            return;
        }

        if ("BANNED".equalsIgnoreCase(totemRules.mode)) {
            event.setCancelled(true);
            return;
        }

        final boolean isCombat = lunatech.strength.integration.PvPManagerHook.isTaggedInCombat(player);

        if (totemRules.preventInCombat && isCombat) {
            event.setCancelled(true);
            return;
        }

        final long now = System.currentTimeMillis();
        if (now < getTotemCooldownUntil(player)) {
            if (!totemRules.quotaOnlyInCombat || isCombat) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(@NotNull EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getItem().getItemStack().getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }

        final RulesConfig.TotemRules totemRules = getTotemRules();
        if (!totemRules.enabled) {
            return;
        }

        if ("BANNED".equalsIgnoreCase(totemRules.mode)) {
            event.setCancelled(true);
            return;
        }

        final boolean isCombat = lunatech.strength.integration.PvPManagerHook.isTaggedInCombat(player);

        if (totemRules.preventInCombat && isCombat) {
            event.setCancelled(true);
            return;
        }

        final long now = System.currentTimeMillis();
        if (now < getTotemCooldownUntil(player)) {
            if (!totemRules.quotaOnlyInCombat || isCombat) {
                event.setCancelled(true);
                return;
            }
        }

        if (totemRules.maxInInventory > 0) {
            final int currentTotems = countPlayerInventoryTotems(player);
            final int pickupAmount = event.getItem().getItemStack().getAmount();
            if (currentTotems + pickupAmount > totemRules.maxInInventory) {
                event.setCancelled(true);
                player.sendMessage(
                    ColorParser.of(totemRules.maxLimitReachedMessage)
                        .with("count", String.valueOf(totemRules.maxInInventory))
                        .build()
                );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(@NotNull InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        final RulesConfig.TotemRules totemRules = getTotemRules();
        if (!totemRules.enabled) {
            return;
        }

        final ItemStack current = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();

        final boolean involvesTotem = (current != null && current.getType() == Material.TOTEM_OF_UNDYING)
            || (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING);

        if (!involvesTotem) {
            return;
        }

        if ("BANNED".equalsIgnoreCase(totemRules.mode)) {
            event.setCancelled(true);
            return;
        }

        final boolean isCombat = lunatech.strength.integration.PvPManagerHook.isTaggedInCombat(player);

        if (totemRules.preventInCombat && isCombat) {
            event.setCancelled(true);
            player.sendMessage(ColorParser.of(totemRules.totemInCombatMessage).build());
            return;
        }

        final long now = System.currentTimeMillis();
        if (now < getTotemCooldownUntil(player)) {
            if (!totemRules.quotaOnlyInCombat || isCombat) {
                event.setCancelled(true);
                player.sendMessage(
                    ColorParser.of(totemRules.totemOnCooldownMessage)
                        .with("time", formatTime(getTotemCooldownUntil(player) - now))
                        .build()
                );
                return;
            }
        }

        if (totemRules.maxInInventory > 0) {
            final int currentTotems = countPlayerInventoryTotems(player);

            // Shift-clicking a Totem from a container (e.g. Chest) into player inventory
            if (event.isShiftClick() && event.getClickedInventory() != player.getInventory()) {
                final int amount = current != null ? current.getAmount() : 1;
                if (currentTotems + amount > totemRules.maxInInventory) {
                    event.setCancelled(true);
                    player.sendMessage(
                        ColorParser.of(totemRules.maxLimitReachedMessage)
                            .with("count", String.valueOf(totemRules.maxInInventory))
                            .build()
                    );
                    return;
                }
            }

            // Placing a Totem from cursor into player inventory
            if (cursor != null && cursor.getType() == Material.TOTEM_OF_UNDYING) {
                if (event.getClickedInventory() == player.getInventory()) {
                    if (currentTotems + cursor.getAmount() > totemRules.maxInInventory) {
                        event.setCancelled(true);
                        player.sendMessage(
                            ColorParser.of(totemRules.maxLimitReachedMessage)
                                .with("count", String.valueOf(totemRules.maxInInventory))
                                .build()
                        );
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(@NotNull InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        final RulesConfig.TotemRules totemRules = getTotemRules();
        if (!totemRules.enabled) {
            return;
        }

        final ItemStack oldCursor = event.getOldCursor();
        if (oldCursor == null || oldCursor.getType() != Material.TOTEM_OF_UNDYING) {
            return;
        }

        if ("BANNED".equalsIgnoreCase(totemRules.mode)) {
            event.setCancelled(true);
            return;
        }

        final boolean isCombat = lunatech.strength.integration.PvPManagerHook.isTaggedInCombat(player);

        if (totemRules.preventInCombat && isCombat) {
            event.setCancelled(true);
            return;
        }

        final long now = System.currentTimeMillis();
        if (now < getTotemCooldownUntil(player)) {
            if (!totemRules.quotaOnlyInCombat || isCombat) {
                event.setCancelled(true);
                return;
            }
        }

        if (totemRules.maxInInventory > 0) {
            final int currentTotems = countPlayerInventoryTotems(player);
            if (currentTotems + oldCursor.getAmount() > totemRules.maxInInventory) {
                event.setCancelled(true);
                player.sendMessage(
                    ColorParser.of(totemRules.maxLimitReachedMessage)
                        .with("count", String.valueOf(totemRules.maxInInventory))
                        .build()
                );
            }
        }
    }

    private void scheduleInventoryUpdate(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.updateInventory();
            }
        }, 1L);
    }

    private int countPlayerInventoryTotems(Player player) {
        int count = 0;
        final ItemStack[] contents = player.getInventory().getContents();
        for (ItemStack item : contents) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private int getTotemPopCount(Player player) {
        Integer val = player.getPersistentDataContainer().get(TOTEM_POP_COUNT_KEY, PersistentDataType.INTEGER);
        return val != null ? val : 0;
    }

    private void setTotemPopCount(Player player, int count) {
        player.getPersistentDataContainer().set(TOTEM_POP_COUNT_KEY, PersistentDataType.INTEGER, count);
    }

    private long getTotemCooldownUntil(Player player) {
        Long val = player.getPersistentDataContainer().get(TOTEM_COOLDOWN_UNTIL_KEY, PersistentDataType.LONG);
        return val != null ? val : 0L;
    }

    private void setTotemCooldownUntil(Player player, long timestamp) {
        player.getPersistentDataContainer().set(TOTEM_COOLDOWN_UNTIL_KEY, PersistentDataType.LONG, timestamp);
    }

    private static String formatTime(long millis) {
        long seconds = Math.max(1, millis / 1000L);
        if (seconds < 60) {
            return seconds + "s";
        }
        long minutes = seconds / 60;
        long remainingSecs = seconds % 60;
        if (minutes < 60) {
            return minutes + "m " + remainingSecs + "s";
        }
        long hours = minutes / 60;
        long remainingMins = minutes % 60;
        if (hours < 24) {
            return hours + "h " + remainingMins + "m";
        }
        long days = hours / 24;
        long remainingHours = hours % 24;
        return days + "d " + remainingHours + "h";
    }
}
