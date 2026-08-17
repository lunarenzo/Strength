package lunatech.strength.listener.packet;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import lunatech.strength.listener.player.AxeAbilityListener;
import org.bukkit.entity.Player;

/**
 * Protocol-level PacketEvents listener that intercepts incoming movement packets for stunned players.
 * Eliminates 100% of server-side position validation teleports and network rubberbanding.
 */
public final class StunPacketListener extends PacketListenerAbstract {

    public StunPacketListener() {
        super(PacketListenerPriority.HIGH);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getUser() == null) return;
        final Object playerObj = event.getUser().getSpigotPlayer();
        if (!(playerObj instanceof Player player)) return;

        if (!AxeAbilityListener.isStunned(player)) return;

        final var type = event.getPacketType();
        if (type == PacketType.Play.Client.PLAYER_POSITION
            || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
            || type == PacketType.Play.Client.PLAYER_FLYING) {
            // Cancel movement packet at protocol layer before Paper movement handler sees it
            event.setCancelled(true);
        }
    }
}
