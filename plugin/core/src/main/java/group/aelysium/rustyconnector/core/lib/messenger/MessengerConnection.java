package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.api.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.api.core.packet.IPacket;
import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.api.core.packet.PacketHandler;
import group.aelysium.rustyconnector.api.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.api.core.packet.PacketType;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.net.InetSocketAddress;
import java.util.Map;

public abstract class MessengerConnection {
    protected PacketOrigin origin;
    public MessengerConnection(PacketOrigin origin) {
        this.origin = origin;
    }
}