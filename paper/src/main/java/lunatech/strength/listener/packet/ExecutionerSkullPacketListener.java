package lunatech.strength.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers;
import lunatech.strength.task.AxeUltimateTask;

import java.util.Arrays;

/**
 * PacketEvents listener that intercepts outgoing SET_PASSENGERS packets and strips
 * Axe Ultimate visual skull ItemDisplay passenger entity IDs. This prevents viewer
 * clients from marking the player as a vehicle, preserving 100% of vanilla and custom nametags.
 */
public final class ExecutionerSkullPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_PASSENGERS) {
            final WrapperPlayServerSetPassengers packet = new WrapperPlayServerSetPassengers(event);
            final int hostEntityId = packet.getEntityId();
            final int[] originalPassengers = packet.getPassengers();

            if (originalPassengers != null && originalPassengers.length > 0) {
                final Integer skullDisplayId = AxeUltimateTask.activeSkullPassengers.get(hostEntityId);
                if (skullDisplayId != null) {
                    final int[] filtered = Arrays.stream(originalPassengers)
                        .filter(id -> id != skullDisplayId)
                        .toArray();

                    if (filtered.length != originalPassengers.length) {
                        packet.setPassengers(filtered);
                        event.markForReEncode(true);
                    }
                }
            }
        }
    }
}
