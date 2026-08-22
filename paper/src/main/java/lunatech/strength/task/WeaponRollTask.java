package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.WeaponSettings;
import lunatech.strength.service.StrengthService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Task that runs a visual rolling title animation to assign a random weapon to players.
 */
public final class WeaponRollTask extends BukkitRunnable {
    private final Strength plugin;
    private final Player player;
    private final StrengthService strengthService;
    private final WeaponSettings settings;
    private int ticksLeft;

    public WeaponRollTask(@NotNull Strength plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.strengthService = plugin.getStrengthService();
        this.settings = plugin.getConfigHandler().getConfig().weapons;
        this.ticksLeft = settings.rollSteps;
    }

    public void start() {
        final long interval = Math.max(1L, settings.stepIntervalTicks);
        this.runTaskTimer(plugin, 0L, interval);
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        final MiniMessage mm = MiniMessage.miniMessage();
        final List<String> available = settings.availableWeapons;
        if (available == null || available.isEmpty()) {
            cancel();
            return;
        }

        if (ticksLeft > 1) {
            // Pick random weapon for rolling frame
            final int randomIndex = ThreadLocalRandom.current().nextInt(available.size());
            final String weaponKey = available.get(randomIndex);
            final String weaponDisplay = getWeaponDisplayString(weaponKey);

            final Component mainTitle = mm.deserialize(settings.rollStartTitle);
            final Component subtitle = mm.deserialize(settings.rollSubtitle.replace("<weapon>", weaponDisplay));

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(300), Duration.ofMillis(100))
            );
            player.showTitle(title);

            playConfiguredSound(player, settings.rollTickSound, settings.rollTickVolume, settings.rollTickPitch);
            ticksLeft--;
        } else {
            // Final weapon assignment
            final int finalIndex = ThreadLocalRandom.current().nextInt(available.size());
            final String finalWeapon = available.get(finalIndex);
            final String finalWeaponDisplay = getWeaponDisplayString(finalWeapon);

            strengthService.setAssignedWeapon(player, finalWeapon);

            final Component mainTitle = mm.deserialize(settings.assignedTitle.replace("<weapon>", finalWeaponDisplay));
            final Component subtitle = mm.deserialize(settings.assignedSubtitle.replace("<weapon>", finalWeaponDisplay));

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(500))
            );
            player.showTitle(title);

            // Play completion sound (synced or delayed if configured)
            if (settings.completionSoundDelayTicks > 0) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        playConfiguredSound(player, settings.completionSound, settings.completionSoundVolume, settings.completionSoundPitch);
                    }
                }, settings.completionSoundDelayTicks);
            } else {
                playConfiguredSound(player, settings.completionSound, settings.completionSoundVolume, settings.completionSoundPitch);
            }

            cancel();
        }
    }

    private String getWeaponDisplayString(String weaponKey) {
        final Map<String, String> customMap = settings.weaponCustomMessages;
        if (customMap != null && customMap.containsKey(weaponKey)) {
            return customMap.get(weaponKey);
        }
        return weaponKey.toUpperCase();
    }

    private void playConfiguredSound(Player p, String soundName, float volume, float pitch) {
        if (soundName == null || soundName.isEmpty() || soundName.equalsIgnoreCase("NONE")) {
            return;
        }
        try {
            final Sound sound = Sound.valueOf(soundName.toUpperCase());
            p.playSound(p.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Fallback for custom sound keys
            p.playSound(p.getLocation(), soundName.toLowerCase(), volume, pitch);
        }
    }
}
