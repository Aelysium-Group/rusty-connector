package group.aelysium.rustyconnector.plugin.velocity.lib.connectors;

import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers.MagicLinkPingHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;

import static group.aelysium.rustyconnector.core.lib.packets.PacketType.PING;
import static group.aelysium.rustyconnector.core.lib.packets.PacketType.SEND_PLAYER;

public class VelocityMessengerSubscriber extends MessengerSubscriber {
    public VelocityMessengerSubscriber(RedisClient client) {
        super(client.privateKey());
    }

    @Override
    public void onMessage(String rawMessage) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();
        MessageCacheService messageCacheService = api.services().messageCacheService();

        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Send a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCacheService == null) messageCacheService = new MessageCacheService(1);

        CacheableMessage cachedMessage = null;
        try {
            String decryptedMessage;
            try {
                decryptedMessage = this.cryptor().decrypt(rawMessage);
                cachedMessage = messageCacheService.cacheMessage(decryptedMessage, PacketStatus.UNDEFINED);
            } catch (Exception e) {
                cachedMessage = messageCacheService.cacheMessage(rawMessage, PacketStatus.UNDEFINED);
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, "This message was encrypted using a different private key from what I have!");
                return;
            }

            GenericPacket.Serializer serializer = new GenericPacket.Serializer();
            GenericPacket message = serializer.parseReceived(decryptedMessage);

            if(messageCacheService.ignoredType(message)) messageCacheService.removeMessage(cachedMessage.getSnowflake());
            if(message.origin() == PacketOrigin.PROXY) throw new Exception("Message from the proxy! Ignoring...");
            try {
                cachedMessage.sentenceMessage(PacketStatus.ACCEPTED);
                VelocityMessengerSubscriber.processParameters(message, cachedMessage);
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.loggerGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.address().toString()+" was blocked by the message tunnel!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (NoOutputException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());
            }
        } catch (Exception e) {
            if(cachedMessage == null) cachedMessage = messageCacheService.cacheMessage(rawMessage, PacketStatus.UNDEFINED);

            if(logger.loggerGate().check(GateKey.SAVE_TRASH_MESSAGES))
                cachedMessage.sentenceMessage(PacketStatus.TRASHED, e.getMessage());
            else
                messageCacheService.removeMessage(cachedMessage.getSnowflake());

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("An incoming message was thrown away!");
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    private static void processParameters(GenericPacket message, CacheableMessage cachedMessage) {
        PluginLogger logger = Tinder.get().logger();

        try {
            if(message.type() == PING)           new MagicLinkPingHandler(message).execute();
            if(message.type() == SEND_PLAYER)    new SendPlayerHandler(message).execute();

            cachedMessage.sentenceMessage(PacketStatus.EXECUTED);
        } catch (NullPointerException e) {
            cachedMessage.sentenceMessage(PacketStatus.PARSING_ERROR);

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        } catch (Exception e) {
            cachedMessage.sentenceMessage(PacketStatus.EXECUTING_ERROR, e.getMessage());

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }
}
