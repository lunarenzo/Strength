package lunatech.strength.cooldown;

import lunatech.strength.AbstractStrength;
import lunatech.strength.Reloadable;
import lunatech.strength.cooldown.listener.ListenerHandler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class CooldownHandler implements Reloadable {
    private ListenerHandler listenerHandler;
    private ScheduledTask autoSaveTask;

    @Override
    public void onLoad(AbstractStrength plugin) {
        if (listenerHandler != null)
            return;

        listenerHandler = new ListenerHandler(plugin);
        listenerHandler.onLoad(plugin);
    }

    @Override
    public void onEnable(AbstractStrength plugin) {
        if (listenerHandler == null)
            return;

        listenerHandler.onEnable(plugin);
        autoSaveTask = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, autoSaveTask(plugin), 10L, 10L, TimeUnit.MINUTES);
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
        if (listenerHandler == null)
            return;

        autoSaveTask.cancel();
        listenerHandler.onDisable(plugin);
        Cooldowns.reset();
    }

    private Consumer<ScheduledTask> autoSaveTask(JavaPlugin plugin) {
        return task -> {
            final Instant now = Instant.now();
            for (final Player p : plugin.getServer().getOnlinePlayers()) {
                if (!p.isOnline())
                    continue;

                p.getScheduler().run(plugin, t -> {
                    final PersistentDataContainer pdc = p.getPersistentDataContainer();
                    for (CooldownType type : CooldownType.values()) {
                        final Instant expiresAt = Cooldowns.get(p, type);
                        final NamespacedKey key = new NamespacedKey(plugin, "cooldown_" + type.name().toLowerCase());
                        if (expiresAt != null && now.isBefore(expiresAt)) {
                            pdc.set(key, PersistentDataType.LONG, expiresAt.toEpochMilli());
                        } else {
                            pdc.remove(key);
                        }
                    }
                }, null);
            }
        };
    }
}
