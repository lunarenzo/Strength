package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.config.TridentConfig;
import lunatech.strength.config.BowConfig;
import lunatech.strength.config.ShieldConfig;
import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.config.SwordConfig;
import lunatech.strength.listener.player.TridentAbilityListener;
import lunatech.strength.listener.player.BowAbilityListener;
import lunatech.strength.listener.player.ShieldAbilityListener;
import lunatech.strength.listener.player.CrossbowAbilityListener;
import lunatech.strength.listener.player.SwordAbilityListener;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.TridentUltimateTask;
import lunatech.strength.task.BowBeamTask;
import lunatech.strength.task.ShieldUltimateTask;
import lunatech.strength.task.SwordUltimateTask;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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
        } else if ("bow".equalsIgnoreCase(assignedWeapon)) {
            triggerBowUltimate(player, strengthService);
        } else if ("shield".equalsIgnoreCase(assignedWeapon)) {
            triggerShieldUltimate(player, strengthService);
        } else if ("crossbow".equalsIgnoreCase(assignedWeapon)) {
            triggerCrossbowUltimate(player, strengthService);
        } else if ("sword".equalsIgnoreCase(assignedWeapon)) {
            triggerSwordUltimate(player, strengthService);
        } else if ("axe".equalsIgnoreCase(assignedWeapon)) {
            triggerAxeUltimate(player, strengthService);
        } else {
            player.sendMessage(ColorParser.of("<red>Your assigned weapon (" + assignedWeapon.toUpperCase() + ") does not have an ultimate ability implemented in this phase.</red>").build());
        }
    }

    private void triggerAxeUltimate(Player player, StrengthService strengthService) {
        final lunatech.strength.config.AxeConfig settings = plugin.getConfigHandler().getAxeConfig();
        final int currentStrength = strengthService.getStrength(player);

        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(ColorParser.of("<red>You do not have enough strength to activate your ultimate! (Required: " + settings.ultimateStrengthRequired + ", Current: " + currentStrength + ")</red>").build());
            return;
        }

        final UUID uuid = player.getUniqueId();
        final int currentCharge = lunatech.strength.listener.player.AxeAbilityListener.ultimateHitsMap.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateCritsRequired) {
            player.sendMessage(ColorParser.of("<red>Your ultimate is not charged yet! (Required: " + settings.ultimateCritsRequired + ", Current: " + currentCharge + " critical hits)</red>").build());
            return;
        }

        final org.bukkit.inventory.ItemStack mainhand = player.getInventory().getItemInMainHand();
        if (mainhand == null || !org.bukkit.Tag.ITEMS_AXES.isTagged(mainhand.getType())) {
            player.sendMessage(ColorParser.of("<red>You must be holding an Axe in your main hand to activate this ultimate!</red>").build());
            return;
        }

        if (lunatech.strength.listener.player.AxeAbilityListener.activeUltimateAttackers.getOrDefault(uuid, false)) {
            player.sendMessage(ColorParser.of("<red>Your Axe ultimate is already active!</red>").build());
            return;
        }

        // Reset charge & activate ultimate
        lunatech.strength.listener.player.AxeAbilityListener.ultimateHitsMap.put(uuid, 0);
        lunatech.strength.listener.player.AxeAbilityListener.activeUltimateAttackers.put(uuid, true);

        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage.replace("{seconds}", String.valueOf(settings.ultimateDurationSeconds)).replace("{multiplier}", String.valueOf(settings.damageMultiplier))).build());

        new lunatech.strength.task.AxeUltimateTask(player, plugin, settings.ultimateDurationSeconds).runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerTridentUltimate(Player player, StrengthService strengthService) {
        final TridentConfig settings = plugin.getConfigHandler().getTridentConfig();
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
        double terrainY = loc.getY();
        final Location scan = loc.clone();
        final int minHeight = scan.getWorld().getMinHeight();
        while (scan.getY() > minHeight) {
            if (scan.getBlock().getType().isSolid() || scan.getBlock().getType() == org.bukkit.Material.WATER) {
                terrainY = scan.getY();
                break;
            }
            scan.subtract(0, 1, 0);
        }

        final Location spawnLoc = loc.clone();
        spawnLoc.setY(terrainY + 3.0);

        final ArmorStand vehicle = player.getWorld().spawn(spawnLoc, ArmorStand.class, armorStand -> {
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
        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
    }

    private void triggerBowUltimate(Player player, StrengthService strengthService) {
        final BowConfig settings = plugin.getConfigHandler().getBowConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.BOW) {
            player.sendMessage(ColorParser.of("<red>You must be holding a Bow to activate your ultimate!</red>").build());
            return;
        }

        // 2. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long lastUse = BowAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimateCooldownMessage)
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        // 3. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(
                ColorParser.of("<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>")
                    .with("req", String.valueOf(settings.ultimateStrengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        // 4. Validate Hit Charge Requirement
        final int currentCharge = BowAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            player.sendMessage(
                ColorParser.of("<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> hits)</red>")
                    .with("req", String.valueOf(settings.ultimateHitsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        // 5. Clear Ultimate Charge and record cooldown timestamp
        BowAbilityListener.ultimateHits.put(uuid, 0);
        BowAbilityListener.ultimateCooldowns.put(uuid, now);

        // 6. Trigger Ability Task (Sonic Charge & Beam firing sequence)
        new BowBeamTask(player, settings)
            .runTaskTimer(plugin, 0L, 1L);

        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
    }

    private void triggerShieldUltimate(Player player, StrengthService strengthService) {
        final ShieldConfig settings = plugin.getConfigHandler().getShieldConfig();
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
        final int currentCharge = ShieldAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            player.sendMessage(
                ColorParser.of("<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> blocks)</red>")
                    .with("req", String.valueOf(settings.ultimateHitsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        // 3. Clear Ultimate Charge
        ShieldAbilityListener.ultimateHits.put(uuid, 0);

        // 4. Trigger Ability Task (Bubble Shield & God Mode task)
        new ShieldUltimateTask(player, settings)
            .runTaskTimer(plugin, 0L, 1L);

        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
    }

    private void triggerCrossbowUltimate(Player player, StrengthService strengthService) {
        final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) {
            player.sendMessage(ColorParser.of("<red>You must be holding a Crossbow to activate your ultimate!</red>").build());
            return;
        }

        // 2. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(
                ColorParser.of("<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>")
                    .with("req", String.valueOf(settings.ultimateStrengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        // 3. Validate Hit Charge Requirement
        final UUID uuid = player.getUniqueId();
        final int currentCharge = CrossbowAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            player.sendMessage(
                ColorParser.of("<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> passive hits)</red>")
                    .with("req", String.valueOf(settings.ultimateHitsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        // 4. Clear Ultimate Charge and prime crossbow
        CrossbowAbilityListener.ultimateHits.put(uuid, 0);
        CrossbowAbilityListener.crossbowUltimatePrimed.put(uuid, true);

        // Feedbacks
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
    }

    private void triggerSwordUltimate(Player player, StrengthService strengthService) {
        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
        final int currentStrength = strengthService.getStrength(player);

        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(
                ColorParser.of("<red>You do not have enough strength to activate your ultimate! (Required: <req>, Current: <current>)</red>")
                    .with("req", String.valueOf(settings.ultimateStrengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();
        final int currentCharge = SwordAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            player.sendMessage(
                ColorParser.of("<red>Your ultimate is not charged yet! (Required: <req>, Current: <current> passive crits)</red>")
                    .with("req", String.valueOf(settings.ultimateHitsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || !Tag.ITEMS_SWORDS.isTagged(mainHand.getType())) {
            player.sendMessage(ColorParser.of("<red>You must be holding a sword to activate Dual Wielding!</red>").build());
            return;
        }

        // Clear charge
        SwordAbilityListener.ultimateHits.put(uuid, 0);

        // Save original offhand item if present
        final ItemStack originalOffhand = player.getInventory().getItemInOffHand();
        if (originalOffhand != null && originalOffhand.getType() != org.bukkit.Material.AIR) {
            SwordAbilityListener.originalOffhandItems.put(uuid, originalOffhand.clone());
        }

        // Clone main hand sword to offhand and mark as clone
        final ItemStack clone = mainHand.clone();
        SwordAbilityListener.markAsClone(clone);
        player.getInventory().setItemInOffHand(clone);

        // Apply +100% attack speed attribute modifier (+50% cooldown reduction)
        final AttributeInstance attr = player.getAttribute(Attribute.ATTACK_SPEED);
        if (attr != null) {
            attr.addModifier(new AttributeModifier(new NamespacedKey(plugin, "sword_ult_speed"), 4.0, AttributeModifier.Operation.ADD_NUMBER));
        }

        // Enable active dual wield
        SwordAbilityListener.activeDualWield.put(uuid, true);

        // Start duration task
        new SwordUltimateTask(player, plugin, settings.ultimateDurationSeconds).runTaskTimer(plugin, 0L, 1L);

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 1.0f, 1.2f);
        player.sendMessage(ColorParser.of(settings.ultimateActivatedMessage).build());
    }
}
