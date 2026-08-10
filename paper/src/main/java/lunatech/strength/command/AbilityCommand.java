package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.config.PluginConfig.TridentSettings;
import lunatech.strength.listener.player.TridentAbilityListener;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.TridentUltimateTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Command class for "/ability" that triggers weapon ultimate abilities.
 */
public final class AbilityCommand extends Command {
    private final Strength plugin;

    public AbilityCommand(@NotNull AbstractStrength plugin) {
        this.plugin = (Strength) plugin;
    }

    @Override
    public CommandAPICommand command() {
        return new CommandAPICommand("ability")
            .withHelp("Triggers your weapon's ultimate ability.", "Triggers your weapon's ultimate ability.")
            .withPermission(CommandHandler.BASE_PERM)
            .executesPlayer(this::executeAbility);
    }

    private void executeAbility(Player player, CommandArguments args) {
        final StrengthService strengthService = plugin.getStrengthService();
        final String assignedWeapon = strengthService.getAssignedWeapon(player);

        if (assignedWeapon == null) {
            player.sendMessage(ColorParser.of("<red>You have no weapon assigned! Weapon abilities are disabled.</red>").build());
            return;
        }

        if ("trident".equalsIgnoreCase(assignedWeapon)) {
            triggerTridentUltimate(player, strengthService);
        } else {
            player.sendMessage(ColorParser.of("<red>Your assigned weapon (" + assignedWeapon.toUpperCase() + ") does not have an ultimate ability implemented in this phase.</red>").build());
        }
    }

    private void triggerTridentUltimate(Player player, StrengthService strengthService) {
        final TridentSettings settings = plugin.getConfigHandler().getConfig().weapons.trident;
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(
                ColorParser.of("<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>")
                    .with("req", String.valueOf(settings.ultimateStrengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        // 2. Validate Hit Charge Requirement
        final UUID uuid = player.getUniqueId();
        final int currentCharge = TridentAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            player.sendMessage(
                ColorParser.of("<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>")
                    .with("req", String.valueOf(settings.ultimateHitsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        // 3. Clear Ultimate Charge
        TridentAbilityListener.ultimateHits.put(uuid, 0);

        // 4. Trigger Ability Tasks
        final Location loc = player.getLocation();
        final ArmorStand vehicle = player.getWorld().spawn(loc, ArmorStand.class, armorStand -> {
            armorStand.setInvisible(true);
            armorStand.setGravity(false);
            armorStand.setMarker(true);
            armorStand.setBasePlate(false);
            armorStand.setSmall(true);
            armorStand.setCanPickupItems(false);
            armorStand.setCustomName("TridentWaveVehicle");
            armorStand.setCustomNameVisible(false);
        });

        // Set rider
        vehicle.addPassenger(player);

        // Run repeating task to manage movement and water trail
        new TridentUltimateTask(player, vehicle, settings)
            .runTaskTimer(plugin, 0L, 1L);

        // Play feedback
        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.0f);
        player.sendMessage(ColorParser.of("<blue><bold>RIPTIDE WAVE ACTIVATED!</bold> Riding the waves...</blue>").build());
    }
}
