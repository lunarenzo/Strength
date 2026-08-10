package lunatech.strength.task;

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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Task that runs a visual rolling title to assign a random weapon to first-time players.
 */
public final class WeaponRollTask extends BukkitRunnable {
    private final Player player;
    private final List<String> availableWeapons;
    private final StrengthService strengthService;
    private final String rollStartTitle;
    private int ticksLeft = 15;

    public WeaponRollTask(
        @NotNull Player player,
        @NotNull List<String> availableWeapons,
        @NotNull StrengthService strengthService,
        @NotNull String rollStartTitle
    ) {
        this.player = player;
        this.availableWeapons = availableWeapons;
        this.strengthService = strengthService;
        this.rollStartTitle = rollStartTitle;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }

        if (ticksLeft > 1) {
            // Show rolling title
            final int randomIndex = ThreadLocalRandom.current().nextInt(availableWeapons.size());
            final String weapon = availableWeapons.get(randomIndex);

            final Component mainTitle = MiniMessage.miniMessage().deserialize(rollStartTitle);
            final Component subtitle = MiniMessage.miniMessage().deserialize("<gray>ROLLING: <gold>" + weapon.toUpperCase() + "</gold></gray>");

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(300), Duration.ofMillis(100))
            );
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            ticksLeft--;
        } else {
            // Final weapon assignment
            final int finalIndex = ThreadLocalRandom.current().nextInt(availableWeapons.size());
            final String finalWeapon = availableWeapons.get(finalIndex);

            strengthService.setAssignedWeapon(player, finalWeapon);

            final Component mainTitle = MiniMessage.miniMessage().deserialize("<gold><bold>" + finalWeapon.toUpperCase() + "</bold></gold>");
            final Component subtitle = MiniMessage.miniMessage().deserialize("<green>Weapon Assigned!</green>");

            final Title title = Title.title(
                mainTitle,
                subtitle,
                Title.Times.times(Duration.ofMillis(200), Duration.ofMillis(2000), Duration.ofMillis(500))
            );
            player.showTitle(title);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            cancel();
        }
    }
}
