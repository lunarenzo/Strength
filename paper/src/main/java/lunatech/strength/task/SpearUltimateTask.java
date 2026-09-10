package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.SpearConfig;
import lunatech.strength.listener.player.SpearAbilityListener;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing active Spear Ultimate (Relentless Thrust).
 * Auto-enchants held Spear, removes hunger drain on lunge, and strips temporary enchantments on expiration.
 */
public final class SpearUltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final SpearConfig.UltimateConfig settings;
    private final int totalTicks;
    private int elapsedTicks = 0;

    public SpearUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull SpearConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
        this.totalTicks = settings.durationSeconds * 20;
    }

    public void launch() {
        final UUID uuid = player.getUniqueId();
        SpearAbilityListener.activeUltimatePlayers.put(uuid, System.currentTimeMillis());

        final Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, Sound.ITEM_ARMOR_EQUIP_CHAIN, 1.5f, 1.2f);
            loc.getWorld().spawnParticle(Particle.CRIT, loc.clone().add(0, 1.0, 0), 10, 0.4, 0.4, 0.4, 0.1);
        }

        MessageUtil.send(player, settings.ultimateActivatedMessage, "duration", String.valueOf(settings.durationSeconds));

        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        elapsedTicks++;
        final UUID uuid = player.getUniqueId();

        final String assignedWeapon = plugin.getStrengthService().getAssignedWeapon(player);
        final boolean isValidState = player.isOnline() && !player.isDead() && "spear".equalsIgnoreCase(assignedWeapon);

        if (!isValidState || elapsedTicks >= (totalTicks * plugin.getStrengthService().getScale())) {
            SpearAbilityListener.activeUltimatePlayers.remove(uuid);
            SpearAbilityListener.stripTemporaryEnchantmentsFromPlayer(player, settings);

            if (player.isOnline()) {
                MessageUtil.send(player, settings.ultimateExpiredMessage);
            }

            cancel();
            return;
        }

        // Apply auto-enchantments to held Spear
        SpearAbilityListener.applyTemporaryEnchantments(player, settings);

        // Visual thrust particle trail
        final Location loc = player.getLocation();
        if (loc.getWorld() != null && elapsedTicks % 4 == 0) {
            loc.getWorld().spawnParticle(Particle.ENCHANTED_HIT, loc.clone().add(0, 1.0, 0), 4, 0.3, 0.5, 0.3, 0.05);
        }
    }
}
