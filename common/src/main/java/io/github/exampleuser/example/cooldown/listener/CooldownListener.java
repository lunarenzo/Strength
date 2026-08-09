package io.github.exampleuser.example.cooldown.listener;

import io.github.exampleuser.example.AbstractExample;
import io.github.exampleuser.example.cooldown.CooldownType;
import io.github.exampleuser.example.cooldown.Cooldowns;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.Instant;

@SuppressWarnings({"unused", "FieldCanBeLocal", "CodeBlock2Expr"})
class CooldownListener implements Listener {
    private final AbstractExample plugin;

    public CooldownListener(AbstractExample plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        final Player player = e.getPlayer();
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        final Instant now = Instant.now();

        for (CooldownType type : CooldownType.values()) {
            final NamespacedKey key = new NamespacedKey(plugin, "cooldown_" + type.name().toLowerCase());
            final Long expiresAtMilli = pdc.get(key, PersistentDataType.LONG);
            if (expiresAtMilli != null) {
                final Instant expiresAt = Instant.ofEpochMilli(expiresAtMilli);
                if (now.isBefore(expiresAt)) {
                    Cooldowns.set(player, type, expiresAt);
                } else {
                    pdc.remove(key);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        final Player player = e.getPlayer();
        final PersistentDataContainer pdc = player.getPersistentDataContainer();
        final Instant now = Instant.now();

        for (CooldownType type : CooldownType.values()) {
            final Instant expiresAt = Cooldowns.get(player, type);
            final NamespacedKey key = new NamespacedKey(plugin, "cooldown_" + type.name().toLowerCase());
            if (expiresAt != null && now.isBefore(expiresAt)) {
                pdc.set(key, PersistentDataType.LONG, expiresAt.toEpochMilli());
            } else {
                pdc.remove(key);
            }
        }
        Cooldowns.removeAll(player);
    }
}
