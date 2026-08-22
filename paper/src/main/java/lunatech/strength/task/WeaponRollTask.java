package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.WeaponSettings;
import lunatech.strength.service.StrengthService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Task that runs a visual rolling title animation to assign a random weapon to players.
 * Features quadratic deceleration easing for a realistic wheel-spinning feel.
 */
public final class WeaponRollTask {
    private final Strength plugin;
    private final Player player;
    private final StrengthService strengthService;
    private final WeaponSettings settings;
    private final int totalSteps;
    private int currentStep = 0;

    public WeaponRollTask(@NotNull Strength plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.strengthService = plugin.getStrengthService();
        this.settings = plugin.getConfigHandler().getConfig().weapons;
        this.totalSteps = Math.max(1, settings.rollSteps);
    }

    public void start() {
        runNextFrame();
    }

    private void runNextFrame() {
        if (!player.isOnline()) {
            return;
        }

        final MiniMessage mm = MiniMessage.miniMessage();
        final List<String> available = settings.availableWeapons;
        if (available == null || available.isEmpty()) {
            return;
        }

        if (currentStep < totalSteps) {
            // Pick random weapon for rolling frame
            final int randomIndex = ThreadLocalRandom.current().nextInt(available.size());
            final String weaponKey = available.get(randomIndex);
            final String weaponDisplay = getWeaponDisplayString(weaponKey);

            final Component mainTitle = mm.deserialize(settings.rollStartTitle);
            final Component subtitle = mm.deserialize(settings.rollSubtitle.replace("<weapon>", weaponDisplay));

            // Calculate current step delay in ticks
            long currentDelay = calculateStepDelay(currentStep);
            
            // Stay duration should span the current delay plus a smooth overlap so text doesn't flicker
            long stayMillis = Math.max(200L, currentDelay * 50L + 200L);

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(stayMillis), Duration.ofMillis(100))
            );
            player.showTitle(title);

            playConfiguredSound(player, settings.rollTickSound, settings.rollTickVolume, settings.rollTickPitch);
            currentStep++;

            // Calculate next frame delay and schedule next execution using Bukkit scheduler lambda
            long nextDelay = calculateStepDelay(currentStep);
            Bukkit.getScheduler().runTaskLater(plugin, this::runNextFrame, Math.max(1L, nextDelay));
        } else {
            // Final weapon assignment frame
            final int finalIndex = ThreadLocalRandom.current().nextInt(available.size());
            final String finalWeapon = available.get(finalIndex);
            final String finalWeaponDisplay = getWeaponDisplayString(finalWeapon);

            strengthService.setAssignedWeapon(player, finalWeapon);

            final Component mainTitle = mm.deserialize(settings.assignedTitle.replace("<weapon>", finalWeaponDisplay));
            final Component subtitle = mm.deserialize(settings.assignedSubtitle.replace("<weapon>", finalWeaponDisplay));

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(3000), Duration.ofMillis(500))
            );
            player.showTitle(title);

            // Play completion sound (synced or delayed if configured)
            if (settings.completionSoundDelayTicks > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        playConfiguredSound(player, settings.completionSound, settings.completionSoundVolume, settings.completionSoundPitch);
                    }
                }, settings.completionSoundDelayTicks);
            } else {
                playConfiguredSound(player, settings.completionSound, settings.completionSoundVolume, settings.completionSoundPitch);
            }
        }
    }

    private long calculateStepDelay(int step) {
        if (totalSteps <= 1) return settings.initialStepDelayTicks;
        double progress = (double) step / (totalSteps - 1);
        double easedProgress = Math.pow(progress, 2.0); // Quadratic easing in (slows down at end)
        double minDelay = Math.max(1, settings.initialStepDelayTicks);
        double maxDelay = Math.max(minDelay, settings.maxStepDelayTicks);
        return Math.round(minDelay + (easedProgress * (maxDelay - minDelay)));
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
