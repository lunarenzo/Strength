package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.ArmorsConfig;
import lunatech.strength.listener.player.ArmorsAbilityListener;
import lunatech.strength.utility.MessageUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Highly optimized task managing active Armors Ultimate (Juggernaut Stance): applies knockback resistance
 * attribute modifier and cleans up state upon expiration or player death/quit.
 */
public final class ArmorsUltimateTask extends BukkitRunnable {
    private static final int CHECK_INTERVAL_TICKS = 20;

    private final Player player;
    private final Strength plugin;
    private final ArmorsConfig.UltimateConfig settings;
    private final int durationTicks;
    private final NamespacedKey modifierKey;
    private int elapsedTicks;

    public ArmorsUltimateTask(@NotNull Player player, @NotNull Strength plugin, @NotNull ArmorsConfig.UltimateConfig settings) {
        this.player = player;
        this.plugin = plugin;
        this.settings = settings;
        this.durationTicks = settings.durationSeconds * 20;
        this.modifierKey = new NamespacedKey(plugin, "armors_ult_knockback");
    }

    public void launch() {
        final UUID uuid = player.getUniqueId();
        ArmorsAbilityListener.activeUltimatePlayers.add(uuid);

        // Apply +1.0 Knockback Resistance (100% knockback immunity)
        final AttributeInstance knockbackAttr = getKnockbackAttributeInstance(player);
        if (knockbackAttr != null) {
            removeKnockbackModifier(knockbackAttr);
            knockbackAttr.addModifier(new AttributeModifier(modifierKey, 1.0, AttributeModifier.Operation.ADD_NUMBER));
        }

        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1.0f, 1.0f);
        MessageUtil.send(player, settings.ultimateActivatedMessage, "duration", String.valueOf(settings.durationSeconds));

        // Run timer every 20 ticks (1s) to eliminate per-tick scheduling overhead (95% CPU savings)
        runTaskTimer(plugin, 0L, CHECK_INTERVAL_TICKS);
    }

    @Override
    public void run() {
        final UUID uuid = player.getUniqueId();

        if (!player.isOnline() || player.isDead() || !ArmorsAbilityListener.activeUltimatePlayers.contains(uuid) || elapsedTicks >= durationTicks) {
            endUltimate();
            cancel();
            return;
        }

        elapsedTicks += CHECK_INTERVAL_TICKS;
    }

    private void endUltimate() {
        final UUID uuid = player.getUniqueId();
        ArmorsAbilityListener.activeUltimatePlayers.remove(uuid);

        final AttributeInstance knockbackAttr = getKnockbackAttributeInstance(player);
        if (knockbackAttr != null) {
            removeKnockbackModifier(knockbackAttr);
        }

        if (player.isOnline() && !player.isDead()) {
            MessageUtil.send(player, settings.ultimateExpiredMessage);
        }
    }

    private AttributeInstance getKnockbackAttributeInstance(Player player) {
        return player.getAttribute(Attribute.KNOCKBACK_RESISTANCE);
    }

    private void removeKnockbackModifier(AttributeInstance attributeInstance) {
        for (AttributeModifier modifier : attributeInstance.getModifiers()) {
            if (modifierKey.equals(modifier.getKey())) {
                attributeInstance.removeModifier(modifier);
            }
        }
    }
}
