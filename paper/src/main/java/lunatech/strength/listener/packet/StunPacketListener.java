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

/**
 * Protocol-level PacketEvents listener that intercepts incoming movement packets for stunned players.
 * Instantly kills client-side jump physics by sending immediate server-side position confirmations.
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
        if (type == PacketType.Play.Client.PLAYER_POSITION
            || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
            || type == PacketType.Play.Client.PLAYER_FLYING) {

            // Cancel client movement/jump attempt
            event.setCancelled(true);

            // Kill client-side jump prediction immediately by sending authoritative position packet
            final Location loc = player.getLocation();
            final WrapperPlayServerPlayerPositionAndLook positionPacket = new WrapperPlayServerPlayerPositionAndLook(
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch(),
                (byte) 0,
                0
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, positionPacket);
        }
    }
}
