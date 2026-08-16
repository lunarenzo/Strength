package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.service.StrengthService;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.List;
import java.util.Random;

/**
 * Task executing the 5-second delayed random weapon assignment roll for first-time joining players.
 */
public final class FirstJoinRollTask extends BukkitRunnable {
    private final Player player;
    private final Strength plugin;
    private final StrengthService strengthService;
    private final List<String> availableWeapons;
    private final Random random = new Random();
    private int step = 0;
    private static final int MAX_STEPS = 20;

    public FirstJoinRollTask(Player player, Strength plugin, StrengthService strengthService, List<String> availableWeapons) {
        this.player = player;
        this.plugin = plugin;
        this.strengthService = strengthService;
        this.availableWeapons = availableWeapons;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (step < MAX_STEPS) {
            final String currentRoll = availableWeapons.get(random.nextInt(availableWeapons.size()));
            final Title title = Title.title(
                ColorParser.of("<gold><bold>ROLLING WEAPON...</bold></gold>").build(),
                ColorParser.of("<yellow>✦ " + currentRoll.toUpperCase() + " ✦</yellow>").build(),
                Title.Times.times(Duration.ZERO, Duration.ofMillis(300), Duration.ZERO)
            );
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1.0f, 1.2f);
            step++;
        } else {
            // Final Roll Assignment
            final String finalWeapon = availableWeapons.get(random.nextInt(availableWeapons.size()));
            strengthService.setAssignedWeapon(player, finalWeapon);

            final Title title = Title.title(
                ColorParser.of("<gold><bold>WEAPON ASSIGNED!</bold></gold>").build(),
                ColorParser.of("<green><bold>⚔ " + finalWeapon.toUpperCase() + " ⚔</bold></green>").build(),
                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofSeconds(1))
            );
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            cancel();
        }
    }
}
