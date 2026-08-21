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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ultra-low latency, zero-allocation PacketEvents listener that maintains real-time vector accumulator
 * tracking for visual skull entities directly on the Netty channel pipeline. Eliminates main-thread location lag
 * and prevents up-and-down bouncing during knockback and vertical jumping.
 */
public final class ExecutionerSkullPacketListener extends PacketListenerAbstract {

    private static final Map<Integer, Vector3d> lastKnownSkullPositions = new ConcurrentHashMap<>();

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
                lastKnownSkullPositions.put(entityId, skullPos);

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
                final Vector3d newSkullPos = updateAccumulatedPosition(entityId, packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
                if (newSkullPos != null) {
                    final Player targetPlayer = AxeUltimateTask.activeSkullTargetPlayersByEntityId.get(entityId);
                    final float yaw = targetPlayer != null && targetPlayer.isOnline() ? targetPlayer.getLocation().getYaw() : 0.0f;
                    final float pitch = targetPlayer != null && targetPlayer.isOnline() ? targetPlayer.getLocation().getPitch() : 0.0f;

                    final WrapperPlayServerEntityTeleport syncTeleport = new WrapperPlayServerEntityTeleport(
                        skullDisplayId,
                        newSkullPos,
                        yaw,
                        pitch,
                        false
                    );
                    event.getUser().sendPacket(syncTeleport);
                }
            }
        } else if (type == PacketType.Play.Server.ENTITY_RELATIVE_MOVE_AND_ROTATION) {
            final WrapperPlayServerEntityRelativeMoveAndRotation packet = new WrapperPlayServerEntityRelativeMoveAndRotation(event);
            final int entityId = packet.getEntityId();
            final Integer skullDisplayId = AxeUltimateTask.activeSkullDisplaysByEntityId.get(entityId);

            if (skullDisplayId != null) {
                final Vector3d newSkullPos = updateAccumulatedPosition(entityId, packet.getDeltaX(), packet.getDeltaY(), packet.getDeltaZ());
                if (newSkullPos != null) {
                    final WrapperPlayServerEntityTeleport syncTeleport = new WrapperPlayServerEntityTeleport(
                        skullDisplayId,
                        newSkullPos,
                        packet.getYaw(),
                        packet.getPitch(),
                        false
                    );
                    event.getUser().sendPacket(syncTeleport);
                }
            }
        }
    }

    private static Vector3d updateAccumulatedPosition(int entityId, double deltaX, double deltaY, double deltaZ) {
        Vector3d lastPos = lastKnownSkullPositions.get(entityId);
        if (lastPos == null) {
            final Player targetPlayer = AxeUltimateTask.activeSkullTargetPlayersByEntityId.get(entityId);
            if (targetPlayer == null || !targetPlayer.isOnline()) {
                return null;
            }
            final double yOffset = AxeUltimateTask.activeSkullYOffsetsByEntityId.getOrDefault(entityId, 2.4);
            final Location loc = targetPlayer.getLocation();
            lastPos = new Vector3d(loc.getX(), loc.getY() + yOffset, loc.getZ());
        }

        final Vector3d newPos = new Vector3d(
            lastPos.getX() + deltaX,
            lastPos.getY() + deltaY,
            lastPos.getZ() + deltaZ
        );
        lastKnownSkullPositions.put(entityId, newPos);
        return newPos;
    }

    public static void removeEntityTracker(int entityId) {
        lastKnownSkullPositions.remove(entityId);
    }
}
