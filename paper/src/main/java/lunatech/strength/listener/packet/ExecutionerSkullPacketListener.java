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

/**
 * High-performance PacketEvents listener that synchronizes visual skull entity movement
 * packets directly on the Netty channel pipeline. This ensures zero 1-tick delay during
 * player movement, jumping, and knockback while preserving 100% of player nametags.
 */
public final class ExecutionerSkullPacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketSend(PacketSendEvent event) {
        final PacketTypeCommon type = event.getPacketType();

        if (type == PacketType.Play.Server.ENTITY_TELEPORT) {
            final WrapperPlayServerEntityTeleport packet = new WrapperPlayServerEntityTeleport(event);
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
        } else if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE) {
            final WrapperPlayServerEntityRelativeMove packet = new WrapperPlayServerEntityRelativeMove(event);
            final int entityId = packet.getEntityId();
            final Integer skullDisplayId = AxeUltimateTask.activeSkullDisplaysByEntityId.get(entityId);

            if (skullDisplayId != null) {
                final WrapperPlayServerEntityRelativeMove syncMove = new WrapperPlayServerEntityRelativeMove(
                    skullDisplayId,
                    packet.getDeltaX(),
                    packet.getDeltaY(),
                    packet.getDeltaZ(),
                    false
                );
                event.getUser().sendPacket(syncMove);
            }
        } else if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            final WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            final int entityId = packet.getEntityId();
            final Integer skullDisplayId = AxeUltimateTask.activeSkullDisplaysByEntityId.get(entityId);

            if (skullDisplayId != null) {
                final WrapperPlayServerEntityRelativeMoveAndRotation syncMoveRot = new WrapperPlayServerEntityRelativeMoveAndRotation(
                    skullDisplayId,
                    packet.getDeltaX(),
                    packet.getDeltaY(),
                    packet.getDeltaZ(),
                    packet.getYaw(),
                    packet.getPitch(),
                    false
                );
                event.getUser().sendPacket(syncMoveRot);
            }
        }
    }
}
