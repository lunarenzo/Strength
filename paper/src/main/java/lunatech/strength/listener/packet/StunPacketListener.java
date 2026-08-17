package lunatech.strength.listener.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerPositionAndLook;
import lunatech.strength.listener.player.AxeAbilityListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protocol-level PacketEvents listener that intercepts incoming movement packets for stunned players.
 * Instantly kills client-side jump physics by sending debounced server-side position confirmations.
 */
public final class StunPacketListener extends PacketListenerAbstract {

    private final Map<UUID, Long> lastTeleportSentMap = new ConcurrentHashMap<>();

    public StunPacketListener() {
        super(PacketListenerPriority.HIGH);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) return;

        final UUID uuid = player.getUniqueId();
        if (!AxeAbilityListener.isStunned(player)) {
            lastTeleportSentMap.remove(uuid);
            return;
        }

        final var type = event.getPacketType();
        if (type == PacketType.Play.Client.PLAYER_POSITION || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            // Cancel client movement/jump attempt
            event.setCancelled(true);

            // Rate-limit position correction packets (max once per 250ms) to prevent packet spam kicks
            final long now = System.currentTimeMillis();
            final long lastSent = lastTeleportSentMap.getOrDefault(uuid, 0L);
            if (now - lastSent >= 250L) {
                lastTeleportSentMap.put(uuid, now);

                final Location loc = player.getLocation();
                final WrapperPlayServerPlayerPositionAndLook positionPacket = new WrapperPlayServerPlayerPositionAndLook(
                    loc.getX(),
                    loc.getY(),
                    loc.getZ(),
                    loc.getYaw(),
                    loc.getPitch(),
                    (byte) 0,
                    0,
                    false
                );

                PacketEvents.getAPI().getPlayerManager().sendPacket(player, positionPacket);
            }
        }
    }
}
