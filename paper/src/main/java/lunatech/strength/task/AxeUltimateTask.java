package lunatech.strength.task;

import lunatech.strength.Strength;
import lunatech.strength.config.AxeConfig;
import lunatech.strength.listener.player.AxeAbilityListener;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

/**
 * Task managing active Axe Ultimate (Executioner's Mark): storing post-mitigation damage
 * for the configured duration and releasing capped burst damage upon expiration.
 */
public final class AxeUltimateTask extends BukkitRunnable {
    private final Player attacker;
    private final Strength plugin;
    private final int durationTicks;
    private int elapsedTicks = 0;

    public AxeUltimateTask(@NotNull Player attacker, @NotNull Strength plugin, int durationSeconds) {
        this.attacker = attacker;
        this.plugin = plugin;
        this.durationTicks = durationSeconds * 20;
    }

    @Override
    public void run() {
        final UUID attackerUuid = attacker.getUniqueId();
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

        if (!attacker.isOnline() || attacker.isDead() || !AxeAbilityListener.activeUltimateAttackers.containsKey(attackerUuid) || elapsedTicks >= durationTicks) {
            endUltimate(attacker, plugin);
            cancel();
            return;
        }

        // Render Actionbar notification for targets accumulating pending damage
        final Map<UUID, Double> damageMap = AxeAbilityListener.storedDamagePools.get(attackerUuid);
        if (damageMap != null) {
            for (Map.Entry<UUID, Double> entry : damageMap.entrySet()) {
                final Player target = plugin.getServer().getPlayer(entry.getKey());
                if (target != null && target.isOnline() && entry.getValue() > 0.0) {
                    final double total = entry.getValue() * settings.damageMultiplier;
                    final String msg = settings.pendingDamageActionbarMessage.replace("{amount}", String.format("%.1f", total));
                    target.sendActionBar(ColorParser.of(msg).build());
                }
            }
        }

        elapsedTicks++;
    }

    private void endUltimate(Player attacker, Strength plugin) {
        final UUID attackerUuid = attacker.getUniqueId();
        AxeAbilityListener.activeUltimateAttackers.remove(attackerUuid);

        final Map<UUID, Double> damageMap = AxeAbilityListener.storedDamagePools.remove(attackerUuid);
        final AxeConfig settings = plugin.getConfigHandler().getAxeConfig();

        if (damageMap != null) {
            for (Map.Entry<UUID, Double> entry : damageMap.entrySet()) {
                final Player target = plugin.getServer().getPlayer(entry.getKey());
                if (target != null && target.isOnline() && !target.isDead()) {
                    final double rawDamage = entry.getValue() * settings.damageMultiplier;

                    // Multi-Totem & Totem Bypass Guardrail: Cap final burst damage to player's current health + absorption
                    // This guarantees exactly ONE totem pops natively without bypassing invulnerability frames.
                    final double currentHealth = target.getHealth();
                    final double absorption = target.getAbsorptionAmount();
                    final double healthPool = currentHealth + absorption;
                    final double finalCappedDamage = Math.min(rawDamage, healthPool);

                    if (finalCappedDamage > 0.0) {
                        target.damage(finalCappedDamage, attacker);
                    }
                }
            }
        }

        if (attacker.isOnline()) {
            attacker.sendMessage(ColorParser.of(settings.ultimateExpiredMessage).build());
        }
    }
}
