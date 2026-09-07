package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.listener.player.MaceAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Task managing Mace Ultimate active state.
 * Auto-enchants held Mace, zeroes smash cooldowns, and safely strips temporary enchantments on expiration.
 */
public final class MaceUltimateTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final MaceConfig.UltimateConfig settings;
    private final int totalTicks;
    private int elapsedTicks = 0;

    public MaceUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull MaceConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
        this.totalTicks = settings.durationSeconds * 20;
    }

    public void launch() {
        final UUID uuid = player.getUniqueId();
        MaceAbilityListener.activeUltimatePlayers.put(uuid, System.currentTimeMillis());

        final Location loc = player.getLocation();
        if (loc.getWorld() != null) {
            loc.getWorld().playSound(loc, Sound.ITEM_MACE_SMASH_AIR, 1.5f, 1.2f);
            loc.getWorld().spawnParticle(Particle.EXPLOSION, loc.clone().add(0, 1.0, 0), 5, 0.3, 0.3, 0.3, 0.05);
        }

        if (settings.ultimateActivatedMessage != null && !settings.ultimateActivatedMessage.isBlank()) {
            final String msg = settings.ultimateActivatedMessage
                .replace("{duration}", String.valueOf(settings.durationSeconds))
                .replace("<duration>", String.valueOf(settings.durationSeconds));
            player.sendMessage(ColorParser.of(msg).build());
        }

        runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public void run() {
        elapsedTicks++;
        final UUID uuid = player.getUniqueId();

        final String assignedWeapon = plugin.getStrengthService().getAssignedWeapon(player);
        final boolean isValidState = player.isOnline() && !player.isDead() && "mace".equalsIgnoreCase(assignedWeapon);

        if (!isValidState || elapsedTicks >= totalTicks) {
            MaceAbilityListener.activeUltimatePlayers.remove(uuid);
            MaceAbilityListener.stripTemporaryEnchantmentsFromPlayer(player, settings);

            if (player.isOnline() && settings.ultimateExpiredMessage != null && !settings.ultimateExpiredMessage.isBlank()) {
                player.sendMessage(ColorParser.of(settings.ultimateExpiredMessage).build());
            }

            cancel();
            return;
        }

        // Apply auto-enchantments to held Mace and zero out smash cooldown
        MaceAbilityListener.applyTemporaryEnchantments(player, settings);
        player.setCooldown(Material.MACE, 0);

        // Visual supercharged particle trail
        final Location loc = player.getLocation();
        if (loc.getWorld() != null && elapsedTicks % 4 == 0) {
            loc.getWorld().spawnParticle(Particle.END_ROD, loc.clone().add(0, 1.0, 0), 3, 0.3, 0.5, 0.3, 0.02);
        }
    }
}
