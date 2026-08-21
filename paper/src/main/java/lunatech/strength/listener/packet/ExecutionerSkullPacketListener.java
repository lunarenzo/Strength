package lunatech.strength.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import lunatech.strength.task.AxeUltimateTask;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * High-performance PacketEvents listener that synchronizes visual skull entity movement
 * packets directly on the Netty channel pipeline. Converts relative movements into absolute
 * teleport packets for ItemDisplay to eliminate client-side delta drift during jumps and knockback.
 */
public final class ExecutionerSkullPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        final PacketTypeCommon type = event.getPacketType();

        if (type == PacketType.Play.Server.ENTITY_TELEPORT) {
            final WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event.clone());
            final int entityId = packet.getEntityId();
            final Integer skullDisplayId = AxeUltimateTask.activeSkullDisplaysByEntityId.get(entityId);

            if (skullDisplayId != null) {
                final double yOffset = AxeUltimateTask.activeSkullYOffsetsByEntityId.getOrDefault(entityId, 2.4);
                final Vector3d origPos = packet.getPosition();
                final Vector3d skullPos = new Vector3d(origPos.getX(), origPos.getY() + yOffset, origPos.getZ());

                final WrapperPlayServerEntityTeleport syncTeleport = new WrapperPlayServerEntityTeleport(
                    skullDisplayId,
                    skullPos,
                    packet.getYaw(),
                    packet.getPitch(),
                    false
                );
                event.getUser().sendPacket(syncTeleport);
            }
        } else if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE || type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            final WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event.clone());
            final int entityId = packet.getEntityId();
            final Integer skullDisplayId = AxeUltimateTask.activeSkullDisplaysByEntityId.get(entityId);

            if (skullDisplayId != null) {
                final Player targetPlayer = AxeUltimateTask.activeSkullTargetPlayersByEntityId.get(entityId);
                if (targetPlayer != null && targetPlayer.isOnline()) {
                    final double yOffset = AxeUltimateTask.activeSkullYOffsetsByEntityId.getOrDefault(entityId, 2.4);
                    final Location loc = targetPlayer.getLocation();

                    final double targetX = loc.getX() + packet.getDeltaX();
                    final double targetY = loc.getY() + packet.getDeltaY() + yOffset;
                    final double targetZ = loc.getZ() + packet.getDeltaZ();

                    float yaw = loc.getYaw();
                    float pitch = loc.getPitch();

                    if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
                        final WrapperPlayServerEntityRelativeMoveAndRotation rotPacket = new WrapperPlayServerEntityRelativeMoveAndRotation(event.clone());
                        yaw = rotPacket.getYaw();
                        pitch = rotPacket.getPitch();
                    }

                    final WrapperPlayServerEntityTeleport syncTeleport = new WrapperPlayServerEntityTeleport(
                        skullDisplayId,
                        new Vector3d(targetX, targetY, targetZ),
                        yaw,
                        pitch,
                        false
                    );

                    event.getUser().sendPacket(syncTeleport);
                }
            }
        }
    }
}
