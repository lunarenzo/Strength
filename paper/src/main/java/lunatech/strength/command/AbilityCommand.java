package lunatech.strength.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.CommandArguments;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.config.ArmorsConfig;
import lunatech.strength.config.AxeConfig;
import lunatech.strength.config.BowConfig;
import lunatech.strength.config.CrossbowConfig;
import lunatech.strength.config.Crossbow2Config;
import lunatech.strength.config.PluginConfig.MessagesConfig;
import lunatech.strength.config.MaceConfig;
import lunatech.strength.config.ShieldConfig;
import lunatech.strength.config.SpearConfig;
import lunatech.strength.config.SwordConfig;
import lunatech.strength.config.Trident2Config;
import lunatech.strength.config.TridentConfig;
import lunatech.strength.integration.WorldGuardHook;
import lunatech.strength.listener.player.ArmorsAbilityListener;
import lunatech.strength.listener.player.AxeAbilityListener;
import lunatech.strength.listener.player.BowAbilityListener;
import lunatech.strength.listener.player.CrossbowAbilityListener;
import lunatech.strength.listener.player.Crossbow2AbilityListener;
import lunatech.strength.listener.player.MaceAbilityListener;
import lunatech.strength.listener.player.ShieldAbilityListener;
import lunatech.strength.listener.player.SpearAbilityListener;
import lunatech.strength.listener.player.SwordAbilityListener;
import lunatech.strength.listener.player.Trident2AbilityListener;
import lunatech.strength.listener.player.TridentAbilityListener;
import lunatech.strength.service.StrengthService;
import lunatech.strength.task.ArmorsUltimateTask;
import lunatech.strength.task.AxeUltimateTask;
import lunatech.strength.task.BowBeamTask;
import lunatech.strength.task.Crossbow2UltimateTask;
import lunatech.strength.task.MaceUltimateTask;
import lunatech.strength.task.ShieldUltimateTask;
import lunatech.strength.task.SpearUltimateTask;
import lunatech.strength.task.SwordUltimateTask;
import lunatech.strength.task.Trident2UltimateTask;
import lunatech.strength.task.TridentUltimateTask;
import lunatech.strength.utility.MessageUtil;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
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
        final MessagesConfig messages = plugin.getConfigHandler().getConfig().messages;

        if (assignedWeapon == null) {
            MessageUtil.send(player, messages.noWeaponAssignedMessage);
            return;
        }

        if (plugin.getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            if (!WorldGuardHook.isAbilityAllowed(plugin, player, player.getLocation())) {
                player.sendMessage(ColorParser.of("<red>You cannot use weapon abilities in this region!</red>").build());
                return;
            }
        }

        if ("trident".equalsIgnoreCase(assignedWeapon)) {
            triggerTridentUltimate(player, strengthService);
        } else if ("trident2".equalsIgnoreCase(assignedWeapon)) {
            triggerTrident2Ultimate(player, strengthService);
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
        } else if ("mace".equalsIgnoreCase(assignedWeapon)) {
            triggerMaceUltimate(player, strengthService);
        } else if ("armors".equalsIgnoreCase(assignedWeapon) || "armor".equalsIgnoreCase(assignedWeapon)) {
            triggerArmorsUltimate(player, strengthService);
        } else if ("spear".equalsIgnoreCase(assignedWeapon)) {
            triggerSpearUltimate(player, strengthService);
        } else if ("crossbow2".equalsIgnoreCase(assignedWeapon)) {
            triggerCrossbow2Ultimate(player, strengthService);
        } else {
            MessageUtil.send(
                player,
                messages.weaponNoUltimateMessage,
                "weapon", assignedWeapon.toUpperCase()
            );
        }
    }

    private void triggerSpearUltimate(Player player, StrengthService strengthService) {
        final SpearConfig settings = plugin.getConfigHandler().getSpearConfig();

        if (settings == null || !settings.enabled || !settings.ultimate.enabled) {
            final String disabledMsg = (settings != null && settings.ultimate != null && settings.ultimate.ultimateDisabledMessage != null)
                ? settings.ultimate.ultimateDisabledMessage
                : "<red>Spear ultimate ability is currently disabled!</red>";
            player.sendMessage(ColorParser.of(disabledMsg).build());
            return;
        }

        if (!SpearAbilityListener.isSpear(player.getInventory().getItemInMainHand())) {
            player.sendMessage(ColorParser.of(settings.ultimate.mustHoldSpearMessage).build());
            return;
        }

        final int currentStrength = strengthService.getStrength(player);
        if (currentStrength < settings.ultimate.strengthRequired) {
            player.sendMessage(
                ColorParser.of(settings.ultimate.notEnoughStrengthMessage
                    .replace("<req>", String.valueOf(settings.ultimate.strengthRequired))
                    .replace("<current>", String.valueOf(currentStrength)))
                    .with("req", String.valueOf(settings.ultimate.strengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();
        final int currentHits = SpearAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentHits < settings.ultimate.hitsRequired) {
            player.sendMessage(
                ColorParser.of(settings.ultimate.notChargedMessage
                    .replace("<req>", String.valueOf(settings.ultimate.hitsRequired))
                    .replace("<current>", String.valueOf(currentHits)))
                    .with("req", String.valueOf(settings.ultimate.hitsRequired))
                    .with("current", String.valueOf(currentHits))
                    .build()
            );
            return;
        }

        final long lastUse = SpearAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimate.cooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimate.ultimateCooldownMessage
                    .replace("<seconds>", String.valueOf(secondsLeft))
                    .replace("{seconds}", String.valueOf(secondsLeft)))
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        // Activate Spear Ultimate
        SpearAbilityListener.ultimateHits.put(uuid, 0);
        SpearAbilityListener.ultimateCooldowns.put(uuid, now);

        new SpearUltimateTask(player, plugin, settings.ultimate).launch();
    }

    private void triggerCrossbow2Ultimate(Player player, StrengthService strengthService) {
        final Crossbow2Config settings = plugin.getConfigHandler().getCrossbow2Config();

        if (settings == null || !settings.enabled || !settings.ultimate.enabled) {
            final String disabledMsg = (settings != null && settings.ultimate != null && settings.ultimate.ultimateDisabledMessage != null)
                ? settings.ultimate.ultimateDisabledMessage
                : "<red>Crossbow2 ultimate ability is currently disabled!</red>";
            player.sendMessage(ColorParser.of(disabledMsg).build());
            return;
        }

        if (!Crossbow2AbilityListener.isCrossbow(player.getInventory().getItemInMainHand())) {
            player.sendMessage(ColorParser.of(settings.ultimate.mustHoldCrossbowMessage).build());
            return;
        }

        final int currentStrength = strengthService.getStrength(player);
        if (currentStrength < settings.ultimate.strengthRequired) {
            player.sendMessage(
                ColorParser.of(settings.ultimate.notEnoughStrengthMessage
                    .replace("<req>", String.valueOf(settings.ultimate.strengthRequired))
                    .replace("<current>", String.valueOf(currentStrength)))
                    .with("req", String.valueOf(settings.ultimate.strengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();
        final long lastUse = Crossbow2AbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimate.cooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimate.ultimateCooldownMessage
                    .replace("<seconds>", String.valueOf(secondsLeft))
                    .replace("{seconds}", String.valueOf(secondsLeft)))
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        // Activate Crossbow2 Ultimate
        Crossbow2AbilityListener.ultimateCooldowns.put(uuid, now);

        new Crossbow2UltimateTask(player, plugin, settings.ultimate).launch();
    }

    private void triggerArmorsUltimate(Player player, StrengthService strengthService) {
        final ArmorsConfig settings = plugin.getConfigHandler().getArmorsConfig();

        if (settings == null || !settings.enabled || !settings.ultimate.enabled) {
            player.sendMessage(ColorParser.of("<red>Armors ultimate ability is currently disabled!</red>").build());
            return;
        }

        if (!ArmorsAbilityListener.hasFullArmorSet(player, settings.passive.upgrades)) {
            player.sendMessage(ColorParser.of(settings.ultimate.mustEquipFullSetMessage).build());
            return;
        }

        final int currentStrength = strengthService.getStrength(player);
        if (currentStrength < settings.ultimate.strengthRequired) {
            player.sendMessage(
                ColorParser.of(settings.ultimate.notEnoughStrengthMessage
                    .replace("<req>", String.valueOf(settings.ultimate.strengthRequired))
                    .replace("<current>", String.valueOf(currentStrength)))
                    .with("req", String.valueOf(settings.ultimate.strengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();
        final long lastUse = ArmorsAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimate.cooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimate.ultimateCooldownMessage
                    .replace("<seconds>", String.valueOf(secondsLeft))
                    .replace("{seconds}", String.valueOf(secondsLeft)))
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        // Activate Armors Ultimate
        ArmorsAbilityListener.ultimateCooldowns.put(uuid, now);

        new ArmorsUltimateTask(player, plugin, settings.ultimate).launch();
    }

    private void triggerMaceUltimate(Player player, StrengthService strengthService) {
        final MaceConfig settings = plugin.getConfigHandler().getMaceConfig();

        if (!settings.enabled || !settings.ultimate.enabled) {
            player.sendMessage(ColorParser.of("<red>Mace ultimate ability is currently disabled!</red>").build());
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.MACE) {
            player.sendMessage(ColorParser.of(settings.ultimate.mustHoldMaceMessage).build());
            return;
        }

        final int currentStrength = strengthService.getStrength(player);
        if (currentStrength < settings.ultimate.strengthRequired) {
            player.sendMessage(
                ColorParser.of(settings.ultimate.notEnoughStrengthMessage
                    .replace("<req>", String.valueOf(settings.ultimate.strengthRequired))
                    .replace("<current>", String.valueOf(currentStrength)))
                    .with("req", String.valueOf(settings.ultimate.strengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();

        final long lastUse = MaceAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimate.cooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimate.ultimateCooldownMessage
                    .replace("<seconds>", String.valueOf(secondsLeft))
                    .replace("{seconds}", String.valueOf(secondsLeft)))
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        // Activate Mace Ultimate
        MaceAbilityListener.ultimateCooldowns.put(uuid, now);

        new MaceUltimateTask(player, plugin, settings.ultimate).launch();
    }

    private void triggerAxeUltimate(Player player, StrengthService strengthService) {
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();
        final int currentStrength = strengthService.getStrength(player);

        if (currentStrength < settings.ultimateStrengthRequired) {
            player.sendMessage(
                ColorParser.of(settings.notEnoughStrengthMessage
                    .replace("{req}", String.valueOf(settings.ultimateStrengthRequired))
                    .replace("{current}", String.valueOf(currentStrength)))
                    .with("req", String.valueOf(settings.ultimateStrengthRequired))
                    .with("current", String.valueOf(currentStrength))
                    .build()
            );
            return;
        }

        final UUID uuid = player.getUniqueId();

        // Check Cooldown Requirement
        final long lastUse = AxeAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            player.sendMessage(
                ColorParser.of(settings.ultimateCooldownMessage
                    .replace("<seconds>", String.valueOf(secondsLeft))
                    .replace("{seconds}", String.valueOf(secondsLeft)))
                    .with("seconds", String.valueOf(secondsLeft))
                    .build()
            );
            return;
        }

        final int currentCharge = AxeAbilityListener.ultimateHitsMap.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateCritsRequired) {
            player.sendMessage(
                ColorParser.of(settings.notChargedMessage
                    .replace("{req}", String.valueOf(settings.ultimateCritsRequired))
                    .replace("{current}", String.valueOf(currentCharge)))
                    .with("req", String.valueOf(settings.ultimateCritsRequired))
                    .with("current", String.valueOf(currentCharge))
                    .build()
            );
            return;
        }

        final ItemStack mainhand = player.getInventory().getItemInMainHand();
        if (mainhand == null || !Tag.ITEMS_AXES.isTagged(mainhand.getType())) {
            MessageUtil.send(player, settings.mustHoldAxeMessage);
            return;
        }

        if (AxeAbilityListener.activeUltimateAttackers.getOrDefault(uuid, false)) {
            MessageUtil.send(player, settings.alreadyActiveMessage);
            return;
        }

        // Reset charge, set cooldown timestamp & activate ultimate
        AxeAbilityListener.ultimateHitsMap.put(uuid, 0);
        AxeAbilityListener.ultimateCooldowns.put(uuid, now);
        AxeAbilityListener.activeUltimateAttackers.put(uuid, true);

        MessageUtil.send(
            player,
            settings.ultimateActivatedMessage
                .replace("{seconds}", String.valueOf(settings.ultimateDurationSeconds))
                .replace("{multiplier}", String.valueOf(settings.damageMultiplier))
        );

        new AxeUltimateTask(player, plugin, settings.ultimateDurationSeconds).runTaskTimer(plugin, 0L, 1L);
    }

    private void triggerTridentUltimate(Player player, StrengthService strengthService) {
        final TridentConfig settings = plugin.getConfigHandler().getTridentConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.TRIDENT) {
            MessageUtil.send(player, settings.mustHoldTridentMessage);
            return;
        }

        // 2. Validate Ground / Water Requirement (matching Poseidon Mod requirement)
        if (!player.isOnGround() && !player.isInWater()) {
            MessageUtil.send(player, settings.mustBeOnGroundMessage);
            return;
        }

        // 3. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long lastUse = TridentAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        // 4. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            MessageUtil.send(
                player,
                settings.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimateStrengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        // 5. Validate Hit Charge Requirement
        final int currentCharge = TridentAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            MessageUtil.send(
                player,
                settings.notChargedMessage,
                Map.of("req", String.valueOf(settings.ultimateHitsRequired), "current", String.valueOf(currentCharge))
            );
            return;
        }

        // 6. Clear Ultimate Charge and record cooldown timestamp
        TridentAbilityListener.ultimateHits.put(uuid, 0);
        TridentAbilityListener.ultimateCooldowns.put(uuid, now);

        // 7. Trigger Poseidon's Calling Ability Task
        new TridentUltimateTask(player, settings)
            .runTaskTimer(plugin, 0L, 1L);

        MessageUtil.send(player, settings.ultimateActivatedMessage);
    }

    private void triggerBowUltimate(Player player, StrengthService strengthService) {
        final BowConfig settings = plugin.getConfigHandler().getBowConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.BOW) {
            MessageUtil.send(player, settings.mustHoldBowMessage);
            return;
        }

        // 2. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long lastUse = BowAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        // 3. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            MessageUtil.send(
                player,
                settings.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimateStrengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        // 4. Validate Hit Charge Requirement
        final int currentCharge = BowAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            MessageUtil.send(
                player,
                settings.notChargedMessage,
                Map.of("req", String.valueOf(settings.ultimateHitsRequired), "current", String.valueOf(currentCharge))
            );
            return;
        }

        // 5. Clear Ultimate Charge, set remaining shots, and record cooldown timestamp
        BowAbilityListener.ultimateHits.put(uuid, 0);
        BowAbilityListener.ultimateCooldowns.put(uuid, now);
        BowAbilityListener.remainingUltShots.put(uuid, settings.ultimateBeams);

        // Sound cue for arming ultimate
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.2f);

        MessageUtil.send(player, settings.ultimateActivatedMessage);
    }

    private void triggerShieldUltimate(Player player, StrengthService strengthService) {
        final ShieldConfig settings = plugin.getConfigHandler().getShieldConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement (Main hand or Offhand)
        if (player.getInventory().getItemInMainHand().getType() != Material.SHIELD
            && player.getInventory().getItemInOffHand().getType() != Material.SHIELD) {
            MessageUtil.send(player, settings.mustHoldShieldMessage);
            return;
        }

        // 2. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long lastUse = ShieldAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        // 3. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            MessageUtil.send(
                player,
                settings.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimateStrengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        // 4. Validate Hit Charge Requirement
        final int currentCharge = ShieldAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            MessageUtil.send(
                player,
                settings.notChargedMessage,
                Map.of("req", String.valueOf(settings.ultimateHitsRequired), "current", String.valueOf(currentCharge))
            );
            return;
        }

        // 5. Clear Ultimate Charge and record cooldown timestamp
        ShieldAbilityListener.ultimateHits.put(uuid, 0);
        ShieldAbilityListener.ultimateCooldowns.put(uuid, now);

        // 6. Trigger Ability Task (Bubble Shield & God Mode task)
        new ShieldUltimateTask(player, settings)
            .runTaskTimer(plugin, 0L, 1L);

        MessageUtil.send(player, settings.ultimateActivatedMessage);
    }

    private void triggerCrossbowUltimate(Player player, StrengthService strengthService) {
        final CrossbowConfig settings = plugin.getConfigHandler().getCrossbowConfig();
        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.CROSSBOW) {
            MessageUtil.send(player, settings.mustHoldCrossbowMessage);
            return;
        }

        // 2. Validate Strength Requirement
        if (currentStrength < settings.ultimateStrengthRequired) {
            MessageUtil.send(
                player,
                settings.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimateStrengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        // 3. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long lastUse = CrossbowAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        // 4. Validate Hit Charge Requirement
        final int currentCharge = CrossbowAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            MessageUtil.send(
                player,
                settings.notChargedMessage,
                Map.of("req", String.valueOf(settings.ultimateHitsRequired), "current", String.valueOf(currentCharge))
            );
            return;
        }

        // 5. Clear Ultimate Charge, record cooldown timestamp & prime crossbow
        CrossbowAbilityListener.ultimateHits.put(uuid, 0);
        CrossbowAbilityListener.ultimateCooldowns.put(uuid, now);
        CrossbowAbilityListener.crossbowUltimatePrimed.put(uuid, true);

        // Feedbacks
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1.0f, 1.0f);
        MessageUtil.send(player, settings.ultimateActivatedMessage);
    }

    private void triggerSwordUltimate(Player player, StrengthService strengthService) {
        final SwordConfig settings = plugin.getConfigHandler().getSwordConfig();
        final int currentStrength = strengthService.getStrength(player);

        if (currentStrength < settings.ultimateStrengthRequired) {
            MessageUtil.send(
                player,
                settings.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimateStrengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        final UUID uuid = player.getUniqueId();

        // Validate Cooldown Requirement
        final long lastUse = SwordAbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimateCooldownSeconds * 1000L;
        final long now = System.currentTimeMillis();

        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        final int currentCharge = SwordAbilityListener.ultimateHits.getOrDefault(uuid, 0);
        if (currentCharge < settings.ultimateHitsRequired) {
            MessageUtil.send(
                player,
                settings.notChargedMessage,
                Map.of("req", String.valueOf(settings.ultimateHitsRequired), "current", String.valueOf(currentCharge))
            );
            return;
        }

        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || !Tag.ITEMS_SWORDS.isTagged(mainHand.getType())) {
            MessageUtil.send(player, settings.mustHoldSwordMessage);
            return;
        }

        // Clear charge & record cooldown timestamp
        SwordAbilityListener.ultimateHits.put(uuid, 0);
        SwordAbilityListener.ultimateCooldowns.put(uuid, now);

        // Save original offhand item if present
        final ItemStack originalOffhand = player.getInventory().getItemInOffHand();
        if (originalOffhand != null && originalOffhand.getType() != Material.AIR) {
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
        MessageUtil.send(player, settings.ultimateActivatedMessage);
    }

    private void triggerTrident2Ultimate(Player player, StrengthService strengthService) {
        final Trident2Config settings = plugin.getConfigHandler().getTrident2Config();
        if (!settings.enabled || !settings.ultimate.enabled) {
            return;
        }

        final int currentStrength = strengthService.getStrength(player);

        // 1. Validate Weapon Held Requirement
        if (player.getInventory().getItemInMainHand().getType() != Material.TRIDENT) {
            MessageUtil.send(player, settings.ultimate.mustHoldTridentMessage);
            return;
        }

        // 2. Validate Strength Requirement
        if (currentStrength < settings.ultimate.strengthRequired) {
            MessageUtil.send(
                player,
                settings.ultimate.notEnoughStrengthMessage,
                Map.of("req", String.valueOf(settings.ultimate.strengthRequired), "current", String.valueOf(currentStrength))
            );
            return;
        }

        // 3. Validate Cooldown Requirement
        final UUID uuid = player.getUniqueId();
        final long now = System.currentTimeMillis();
        final long lastUse = Trident2AbilityListener.ultimateCooldowns.getOrDefault(uuid, 0L);
        final long cooldownMillis = settings.ultimate.cooldownSeconds * 1000L;
        if (now - lastUse < cooldownMillis) {
            final long secondsLeft = (cooldownMillis - (now - lastUse)) / 1000L + 1;
            MessageUtil.send(
                player,
                settings.ultimate.ultimateCooldownMessage,
                "seconds", String.valueOf(secondsLeft)
            );
            return;
        }

        // 4. Record active ultimate and cooldown timestamp
        Trident2AbilityListener.ultimateCooldowns.put(uuid, now);
        Trident2AbilityListener.activeUltimatePlayers.put(uuid, now + settings.ultimate.durationSeconds * 1000L);

        // 5. Trigger Thunderstorm Ultimate Task
        new Trident2UltimateTask(player, plugin, settings.ultimate.durationSeconds)
            .runTaskTimer(plugin, 0L, 1L);

        player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.0f);
        MessageUtil.send(player, settings.ultimate.ultimateActivatedMessage);
    }
}
