package lunatech.strength.hook.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import lunatech.strength.AbstractStrength;
import lunatech.strength.Strength;
import lunatech.strength.hook.AbstractHook;
import lunatech.strength.hook.Hook;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

/**
 * A hook that enables API for PacketEvents.
 */
public class PacketEventsHook extends AbstractHook {
    /**
     * Instantiates a new PacketEvents hook.
     *
     * @param plugin the plugin instance
     */
    public PacketEventsHook(Strength plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(AbstractStrength plugin) {
        if (!isPluginPresent(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(getPlugin()));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable(AbstractStrength plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new lunatech.strength.listener.packet.StunPacketListener());
    }

    @Override
    public void onDisable(AbstractStrength plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PacketEvents.getPluginName()) && PacketEvents.getAPI().isLoaded();
    }
}
