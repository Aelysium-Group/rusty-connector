package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.toolkit.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketStatus;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.core.lib.packets.*;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MessengerSubscriber {
    private final AESCryptor cryptor;
    private final PluginLogger logger;
    private IMessageCacheService<? extends ICacheableMessage> messageCache;
    private final Map<PacketType.Mapping, List<PacketListener>> listeners = new HashMap<>();
    private final PacketOrigin origin;
    private final InetSocketAddress originAddress;

    public MessengerSubscriber(AESCryptor cryptor, IMessageCacheService<? extends ICacheableMessage> messageCache, PluginLogger logger, PacketOrigin origin, InetSocketAddress originAddress) {
        this.cryptor = cryptor;
        this.messageCache = messageCache;
        this.logger = logger;
        this.origin = origin;
        if(this.origin == PacketOrigin.PROXY)
            this.originAddress = null;
        else
            this.originAddress = originAddress;
    }

    public AESCryptor cryptor() { return this.cryptor; }

    public void listen(PacketListener listener) {
        this.listeners.computeIfAbsent(listener.identifier(), s -> new ArrayList<>());

        this.listeners.get(listener.identifier()).add(listener);
    }

    protected void onMessage(String rawMessage) {
        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Set a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCache == null) messageCache = new MessageCacheService(1);

        ICacheableMessage cachedMessage = null;
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

                List<PacketListener> listeners = this.listeners.get(message.type());
                if(listeners.isEmpty()) throw new NullPointerException("No packet handler with the type "+message.type().name()+" exists!");

                listeners.forEach(listener -> {
                    try {
                        listener.execute(message);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
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
