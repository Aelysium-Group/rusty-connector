package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.hash.AESCryptor;
import group.aelysium.rustyconnector.api.velocity.log_gate.GateKey;
import group.aelysium.rustyconnector.core.lib.packets.*;

import java.net.InetSocketAddress;
import java.util.Map;

public abstract class MessengerSubscriber {
    private final AESCryptor cryptor;
    private final PluginLogger logger;
    private MessageCacheService messageCache;
    private Map<PacketType.Mapping, PacketHandler> handlers;
    private PacketOrigin origin;
    private InetSocketAddress originAddress;

    public MessengerSubscriber(AESCryptor cryptor, MessageCacheService messageCache, PluginLogger logger, Map<PacketType.Mapping, PacketHandler> handlers, PacketOrigin origin, InetSocketAddress originAddress) {
        this.cryptor = cryptor;
        this.messageCache = messageCache;
        this.logger = logger;
        this.handlers = handlers;
        this.origin = origin;
        if(this.origin == PacketOrigin.PROXY)
            this.originAddress = null;
        else
            this.originAddress = originAddress;
    }

    public AESCryptor cryptor() { return this.cryptor; }

    protected void onMessage(String rawMessage) {
        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Set a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCache == null) messageCache = new MessageCacheService(1);

        CacheableMessage cachedMessage = null;
        try {
            String decryptedMessage;
            try {
                decryptedMessage = this.cryptor().decrypt(rawMessage);
                cachedMessage = messageCache.cacheMessage(decryptedMessage, PacketStatus.UNDEFINED);
            } catch (Exception e) {
                cachedMessage = messageCache.cacheMessage(rawMessage, PacketStatus.UNDEFINED);
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, "This message was encrypted using a different private key from what I have!");
                return;
            }

            GenericPacket.Serializer serializer = new GenericPacket.Serializer();
            GenericPacket message = serializer.parseReceived(decryptedMessage);

            if(messageCache.ignoredType(message)) messageCache.removeMessage(cachedMessage.getSnowflake());
            if(message.origin() == this.origin) throw new Exception("Message from the "+this.origin.name()+"! Ignoring...");
            if(this.origin == PacketOrigin.SERVER)
                if (!this.originAddress.toString().equals(message.address().toString()))
                    throw new Exception("Message is addressed to another server! Ignoring...");
            try {
                cachedMessage.sentenceMessage(PacketStatus.ACCEPTED);

                PacketHandler handler = this.handlers.get(message.type());
                if(handler == null) throw new NullPointerException("No packet handler with the type "+message.type().name()+" exists!");

                handler.execute(message);
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.loggerGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.address().toString()+" was blocked by the message tunnel!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (NoOutputException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());
            }
        } catch (Exception e) {
            if(cachedMessage == null) cachedMessage = messageCache.cacheMessage(rawMessage, PacketStatus.UNDEFINED);

            if(logger.loggerGate().check(GateKey.SAVE_TRASH_MESSAGES))
                cachedMessage.sentenceMessage(PacketStatus.TRASHED, e.getMessage());
            else
                messageCache.removeMessage(cachedMessage.getSnowflake());

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("An incoming message was thrown away!");
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }
}
