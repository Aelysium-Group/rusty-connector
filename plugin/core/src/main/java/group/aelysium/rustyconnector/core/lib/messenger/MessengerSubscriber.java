package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketStatus;
import group.aelysium.rustyconnector.core.lib.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.core.log_gate.GateKey;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

import java.util.*;

public abstract class MessengerSubscriber {
    private final AESCryptor cryptor;
    private final PluginLogger logger;
    private IMessageCacheService<? extends ICacheableMessage> messageCache;
    private final Map<PacketIdentification, List<PacketListener<?>>> listeners = new HashMap<>();
    private final UUID senderUUID;

    public MessengerSubscriber(AESCryptor cryptor, IMessageCacheService<? extends ICacheableMessage> messageCache, PluginLogger logger, UUID senderUUID) {
        this.cryptor = cryptor;
        this.messageCache = messageCache;
        this.logger = logger;
        this.senderUUID = senderUUID;
    }

    public AESCryptor cryptor() { return this.cryptor; }

    public <TPacketListener extends PacketListener<? extends GenericPacket>> void listen(TPacketListener listener) {
        this.listeners.computeIfAbsent(listener.target(), s -> new ArrayList<>());

        this.listeners.get(listener.target()).add(listener);
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

            if (messageCache.ignoredType(message)) messageCache.removeMessage(cachedMessage.getSnowflake());

            if (senderUUID == null) { // If senderUUID is null it means we are the proxy
                if (message.target() != null) throw new Exception("Message was not addressed to us.");
            } else {
                if(message.target() == null) throw new Exception("Message was not addressed to us.");
                if(!message.target().equals(senderUUID)) throw new Exception("Message was not addressed to us.");
            }

            try {
                cachedMessage.sentenceMessage(PacketStatus.ACCEPTED);

                List<PacketListener<? extends GenericPacket>> listeners = this.listeners.get(message.identification());
                if(listeners.isEmpty()) throw new NullPointerException("No packet handler with the type "+message.identification()+" exists!");

                listeners.forEach(listener -> {
                    try {
                        listener.genericExecute(message);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.loggerGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.sender().toString()+" was blocked by the message tunnel!");
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
