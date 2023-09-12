package group.aelysium.rustyconnector.core.lib.connectors.messenger;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.connectors.Connection;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.PacketType;

import java.util.Map;

public abstract class MessengerConnection extends Connection {
    /**
     * Used to recursively subscribe to a remote resource.
     * @param subscriber A class instance of the listener to be used.
     * @throws IllegalStateException If the service is already running.
     */
    protected abstract void subscribe(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers);

    /**
     * Start listening on the messenger connection for messages.
     * @param subscriber A class instance of the listener to be used.
     * @throws IllegalStateException If the service is already running.
     */
    public abstract void startListening(MessageCacheService cache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers);

    /**
     * Publish a new message to the {@link MessengerConnection}.
     * @param message The message to publish.
     */
    public abstract void publish(GenericPacket message);

    /**
     * Validate a private key.
     * @param privateKey The private key that needs to be validated.
     * @return `true` if the key is valid. `false` otherwise.
     */
    public abstract boolean validatePrivateKey(char[] privateKey);
}