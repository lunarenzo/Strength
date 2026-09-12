package lunatech.strength.listener.player;

import lunatech.strength.Strength;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.WeaponRollTask;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Listener that synchronizes strength attributes and handles initial weapon rolls on player join.
 */
public final class PlayerJoinListener implements Listener {
    private final Strength plugin;
    private final StrengthService strengthService;

    public PlayerJoinListener(@NotNull Strength plugin, @NotNull StrengthService strengthService) {
        this.plugin = plugin;
        this.strengthService = strengthService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // Safety check: reset invulnerability status on join
        player.setInvulnerable(false);

        final int strength = strengthService.getStrength(player);
        
        // Re-apply modifier on join to synchronize entity state
        strengthService.applyAttributeModifier(player, strength);

        // Defer spear attack-speed modifier application by 2 ticks: PlayerJoinEvent fires before
        // the server finalizes vanilla item equip attribute modifiers (the item's own attack_speed
        // modifier is applied in the next server tick after the join packet handshake completes).
        // Reading speedAttr.getValue() inside the same tick as join returns the base value only
        // (4.0), making our diff calculation wrong. A 2-tick delay ensures the vanilla pipeline
        // has settled so our ADD_NUMBER modifier correctly bridges to sword speed (1.6).
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                final String assigned = strengthService.getAssignedWeapon(player);
                if ("spear".equalsIgnoreCase(assigned)) {
                    SpearAbilityListener.staticUpdateAttackSpeed(player, plugin, strengthService);
                }
            }
        }, 2L);

        // Check if player has an assigned weapon, if not trigger the rolling process or notify pending roll
        final String assignedWeapon = strengthService.getAssignedWeapon(player);
        if (assignedWeapon == null) {
            final var weaponConfig = plugin.getConfigHandler().getConfig().weapons;
            final boolean isOnDeathReset = "ON_DEATH_RESET".equalsIgnoreCase(weaponConfig.assignmentMode);

            if (isOnDeathReset && player.hasPlayedBefore()) {
                MessageUtil.send(player, plugin.getConfigHandler().getConfig().messages.pendingRollJoinMessage);
                if (!weaponConfig.autoRollOnJoinWhenUnassigned) {
                    return;
                }
            }

            // Check if AuthMe integration is enabled and player is not authenticated yet
            final boolean authmeEnabled = plugin.getConfigHandler().getConfig().authme.enabled;
            if (authmeEnabled && plugin.getServer().getPluginManager().isPluginEnabled("AuthMe")) {
                try {
                    if (!fr.xephi.authme.api.v3.AuthMeApi.getInstance().isAuthenticated(player)) {
                        // Defer weapon roll until AuthMe LoginEvent or RegisterEvent fires
                        return;
                    }
                } catch (Throwable ignored) {
                }
            }

            final List<String> available = weaponConfig.availableWeapons;
            if (available != null && !available.isEmpty()) {
                final int delaySeconds = weaponConfig.rollDelaySeconds;
                
                // Run the roll title effect after the configured delay
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline() && strengthService.getAssignedWeapon(player) == null) {
                        new WeaponRollTask(plugin, player).start();
                    }
                }, Math.max(0L, delaySeconds * 20L));
            }
        }
    }
}
