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
 * Features quadratic deceleration easing for a realistic wheel-spinning feel, and ensures
 * the final roll frame perfectly lands on the assigned weapon.
 */
public final class WeaponRollTask {
    private final Strength plugin;
    private final Player player;
    private final StrengthService strengthService;
    private final WeaponSettings settings;
    private final int totalSteps;
    private final String selectedWeapon;
    private int currentStep = 0;

    public WeaponRollTask(@NotNull Strength plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.strengthService = plugin.getStrengthService();
        this.settings = plugin.getConfigHandler().getConfig().weapons;
        this.totalSteps = Math.max(1, settings.rollSteps);

        // Pre-select the winning weapon upfront so the roll visual lands on it seamlessly
        final List<String> available = settings.availableWeapons;
        if (available != null && !available.isEmpty()) {
            final int chosenIndex = ThreadLocalRandom.current().nextInt(available.size());
            this.selectedWeapon = available.get(chosenIndex);
        } else {
            this.selectedWeapon = "Sword";
        }
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
            final String weaponKey;
            // On the final step of the rolling animation, land ON the pre-selected winning weapon!
            if (currentStep == totalSteps - 1) {
                weaponKey = selectedWeapon;
            } else {
                final int randomIndex = ThreadLocalRandom.current().nextInt(available.size());
                weaponKey = available.get(randomIndex);
            }
            final String weaponDisplay = getWeaponDisplayString(weaponKey);

            final String rawTitle = getTitleText(false, currentStep, weaponDisplay);
            final String rawSubtitle = getSubtitleText(false, currentStep, weaponDisplay);

            final Component mainTitle = mm.deserialize(rawTitle);
            final Component subtitle = mm.deserialize(rawSubtitle);

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
            // Final weapon assignment phase - uses selectedWeapon!
            strengthService.setAssignedWeapon(player, selectedWeapon);

            final List<String> titleFrames = settings.assignedTitleFrames;
            final List<String> subtitleFrames = settings.assignedSubtitleFrames;

            final boolean hasAnimatedAssigned = (titleFrames != null && titleFrames.size() > 1) 
                    || (subtitleFrames != null && subtitleFrames.size() > 1);

            final String finalWeaponDisplay = getWeaponDisplayString(selectedWeapon);

            if (hasAnimatedAssigned) {
                final int delayTicks = Math.max(1, settings.assignedAnimationFrameDelayTicks);
                final int maxFrames = Math.max(1, 60 / delayTicks);
                runAssignedTitleAnimation(0, maxFrames, finalWeaponDisplay);
            } else {
                final String rawTitle = getTitleText(true, 0, finalWeaponDisplay);
                final String rawSubtitle = getSubtitleText(true, 0, finalWeaponDisplay);

                final Component mainTitle = mm.deserialize(rawTitle);
                final Component subtitle = mm.deserialize(rawSubtitle);

                final Title title = Title.title(
                    mainTitle,
                    subtitle,
                    Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(3000), Duration.ofMillis(500))
                );
                player.showTitle(title);
            }

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

    private void runAssignedTitleAnimation(int frame, int maxFrames, String weaponDisplay) {
        if (!player.isOnline() || frame >= maxFrames) {
            return;
        }

        final MiniMessage mm = MiniMessage.miniMessage();
        final String rawTitle = getTitleText(true, frame, weaponDisplay);
        final String rawSubtitle = getSubtitleText(true, frame, weaponDisplay);

        final Component mainTitle = mm.deserialize(rawTitle);
        final Component subtitle = mm.deserialize(rawSubtitle);

        final int delayTicks = Math.max(1, settings.assignedAnimationFrameDelayTicks);
        final long stayMillis = Math.max(200L, delayTicks * 50L + 200L);

        final Title title = Title.title(
            mainTitle,
            subtitle,
            Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(stayMillis), Duration.ofMillis(100))
        );
        player.showTitle(title);

        Bukkit.getScheduler().runTaskLater(plugin, () -> runAssignedTitleAnimation(frame + 1, maxFrames, weaponDisplay), delayTicks);
    }

    private String getTitleText(boolean isAssigned, int frameIndex, String weaponDisplay) {
        String template;
        if (isAssigned) {
            final List<String> frames = settings.assignedTitleFrames;
            if (frames != null && !frames.isEmpty()) {
                template = frames.get(frameIndex % frames.size());
            } else {
                template = settings.assignedTitle;
            }
        } else {
            final List<String> frames = settings.rollStartTitleFrames;
            if (frames != null && !frames.isEmpty()) {
                template = frames.get(frameIndex % frames.size());
            } else {
                template = settings.rollStartTitle;
            }
        }
        return template == null ? "" : template.replace("<weapon>", weaponDisplay);
    }

    private String getSubtitleText(boolean isAssigned, int frameIndex, String weaponDisplay) {
        String template;
        if (isAssigned) {
            final List<String> frames = settings.assignedSubtitleFrames;
            if (frames != null && !frames.isEmpty()) {
                template = frames.get(frameIndex % frames.size());
            } else {
                template = settings.assignedSubtitle;
            }
        } else {
            final List<String> frames = settings.rollSubtitleFrames;
            if (frames != null && !frames.isEmpty()) {
                template = frames.get(frameIndex % frames.size());
            } else {
                template = settings.rollSubtitle;
            }
        }
        return template == null ? "" : template.replace("<weapon>", weaponDisplay);
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
