package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers.MagicLinkPingHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;

import javax.naming.AuthenticationException;

import static group.aelysium.rustyconnector.core.lib.packets.PacketType.PING;
import static group.aelysium.rustyconnector.core.lib.packets.PacketType.SEND_PLAYER;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        MessageCacheService messageCacheService = api.services().messageCacheService();
        MessengerConnection<?> backboneMessenger = api.core().backbone().connection().orElseThrow();

        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Send a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCacheService == null) messageCacheService = new MessageCacheService(1);

        CacheableMessage cachedMessage = messageCacheService.cacheMessage(rawMessage, PacketStatus.UNDEFINED);
        try {
            GenericPacket.Serializer serializer = new GenericPacket.Serializer();
            GenericPacket message = serializer.parseReceived(rawMessage);

            if(messageCacheService.ignoredType(message)) messageCacheService.removeMessage(cachedMessage.getSnowflake());
            if(message.origin() == PacketOrigin.PROXY) throw new Exception("Message from the proxy! Ignoring...");
            try {
                backboneMessenger.validatePrivateKey(message.privateKey());

                if (!(backboneMessenger.validatePrivateKey(message.privateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(PacketStatus.ACCEPTED);
                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());

                logger.error("An incoming message from: "+message.address().toString()+" had an invalid private-key!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.loggerGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.address().toString()+" was blocked by the message tunnel!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (NoOutputException e) {
                cachedMessage.sentenceMessage(PacketStatus.AUTH_DENIAL, e.getMessage());
            }
        } catch (Exception e) {
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
        PluginLogger logger = VelocityAPI.get().logger();

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
