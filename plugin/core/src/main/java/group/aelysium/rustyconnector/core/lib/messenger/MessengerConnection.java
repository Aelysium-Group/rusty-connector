package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.net.InetSocketAddress;
import java.util.Map;

public abstract class MessengerConnection extends Service {
    protected PacketOrigin origin;
    public MessengerConnection(PacketOrigin origin) {
        this.origin = origin;
    }

    /**
     * Used to recursively subscribe to a remote resource.
     * @throws IllegalStateException If the service is already running.
     */
    protected abstract void subscribe(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, InetSocketAddress originAddress);

    /**
     * Start listening on the messenger connection for messages.
     * @throws IllegalStateException If the service is already running.
     */
    public abstract void startListening(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, InetSocketAddress originAddress);

    /**
     * Publish a new message to the {@link MessengerConnection}.
     * @param message The message to publish.
     */
    public abstract void publish(GenericPacket message);
}