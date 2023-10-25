package group.aelysium.rustyconnector.api.core.messenger;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.api.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.api.core.packet.IPacket;
import group.aelysium.rustyconnector.api.core.packet.PacketHandler;
import group.aelysium.rustyconnector.api.core.packet.PacketType;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.net.InetSocketAddress;
import java.util.Map;

public interface IMessengerConnection<TPacket extends IPacket, TCacheableMessage extends ICacheableMessage, TMessageCacheService extends IMessageCacheService<TPacket, TCacheableMessage>> extends Service {
    /**
     * Start listening on the messenger connection for messages.
     *
     * @throws IllegalStateException If the service is already running.
     */
    void startListening(TMessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler<TPacket>> handlers, InetSocketAddress originAddress);

    /**
     * Publish a new message to the {@link IMessengerConnection}.
     *
     * @param message The message to publish.
     */
    <P extends IPacket> void publish(P message);
}
