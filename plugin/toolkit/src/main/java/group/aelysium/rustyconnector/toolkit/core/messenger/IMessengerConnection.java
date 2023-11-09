package group.aelysium.rustyconnector.toolkit.core.messenger;

import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketHandler;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.net.InetSocketAddress;
import java.util.Map;

public interface IMessengerConnection<TMessageCacheService extends IMessageCacheService<? extends ICacheableMessage>> extends Service {
    /**
     * Start listening on the messenger connection for messages.
     *
     * @throws IllegalStateException If the service is already running.
     */
    void startListening(TMessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, InetSocketAddress originAddress);

    /**
     * Publish a new message to the {@link IMessengerConnection}.
     *
     * @param message The message to publish.
     */
    <P extends IPacket> void publish(P message);
}
