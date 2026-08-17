package lunatech.strength.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPosition;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerPositionAndRotation;
import lunatech.strength.listener.player.AxeAbilityListener;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Protocol-level PacketEvents listener that rewrites movement packets for stunned players.
 * Eliminates outbound teleport replies completely, eliminating any chance of packet rate kicks.
 */
public final class StunPacketListener extends PacketListenerAbstract {

    public StunPacketListener() {
        super(PacketListenerPriority.HIGH);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        final Object playerObj = event.getPlayer();
        if (!(playerObj instanceof Player player)) return;

        if (!AxeAbilityListener.isStunned(player)) return;

        final var type = event.getPacketType();
        final Location serverLoc = player.getLocation();
        final Vector3d pos = new Vector3d(serverLoc.getX(), serverLoc.getY(), serverLoc.getZ());

        if (type == PacketType.Play.Client.PLAYER_POSITION) {
            final WrapperPlayClientPlayerPosition packet = new WrapperPlayClientPlayerPosition(event);
            packet.setPosition(pos);
            packet.setOnGround(true);
        } else if (type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
            final WrapperPlayClientPlayerPositionAndRotation packet = new WrapperPlayClientPlayerPositionAndRotation(event);
            packet.setPosition(pos);
            packet.setOnGround(true);
            // Yaw and pitch are preserved so head turning stays 100% smooth!
        }

        // PacketType.Play.Client.PLAYER_FLYING is left untouched to keep network heartbeat alive!
    }
}
