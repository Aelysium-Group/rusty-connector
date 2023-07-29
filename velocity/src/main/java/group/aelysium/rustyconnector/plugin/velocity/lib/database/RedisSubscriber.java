package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageOrigin;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.exception.BlockedMessageException;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers.MagicLinkPingHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;

import javax.naming.AuthenticationException;

import static group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType.*;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.database.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        VelocityAPI api = VelocityAPI.get();
        PluginLogger logger = api.logger();
        MessageCacheService messageCacheService = api.services().messageCacheService();
        RedisService redisService = api.services().redisService();

        // If the proxy doesn't have a message cache (maybe it's in the middle of a reload)
        // Send a temporary, worthless, message cache so that the system can still "cache" messages into the worthless cache if needed.
        if(messageCacheService == null) messageCacheService = new MessageCacheService(1);

        CacheableMessage cachedMessage = messageCacheService.cacheMessage(rawMessage, MessageStatus.UNDEFINED);
        try {
            GenericRedisMessage.Serializer serializer = new GenericRedisMessage.Serializer();
            GenericRedisMessage message = serializer.parseReceived(rawMessage);

            if(messageCacheService.ignoredType(message)) messageCacheService.removeMessage(cachedMessage.getSnowflake());
            if(message.origin() == MessageOrigin.PROXY) throw new Exception("Message from the proxy! Ignoring...");
            try {
                redisService.validatePrivateKey(message.privateKey());

                if (!(redisService.validatePrivateKey(message.privateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");

                cachedMessage.sentenceMessage(MessageStatus.ACCEPTED);
                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());

                logger.error("An incoming message from: "+message.address().toString()+" had an invalid private-key!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (BlockedMessageException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());

                if(!logger.loggerGate().check(GateKey.MESSAGE_TUNNEL_FAILED_MESSAGE)) return;

                logger.error("An incoming message from: "+message.address().toString()+" was blocked by the message tunnel!");
                logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
            } catch (NoOutputException e) {
                cachedMessage.sentenceMessage(MessageStatus.AUTH_DENIAL, e.getMessage());
            }
        } catch (Exception e) {
            if(logger.loggerGate().check(GateKey.SAVE_TRASH_MESSAGES))
                cachedMessage.sentenceMessage(MessageStatus.TRASHED, e.getMessage());
            else
                messageCacheService.removeMessage(cachedMessage.getSnowflake());

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("An incoming message was thrown away!");
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }

    private static void processParameters(GenericRedisMessage message, CacheableMessage cachedMessage) {
        PluginLogger logger = VelocityAPI.get().logger();

        try {
            if(message.type() == PING)           new MagicLinkPingHandler(message).execute();
            if(message.type() == SEND_PLAYER)    new SendPlayerHandler(message).execute();

            cachedMessage.sentenceMessage(MessageStatus.EXECUTED);
        } catch (NullPointerException e) {
            cachedMessage.sentenceMessage(MessageStatus.PARSING_ERROR);

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        } catch (Exception e) {
            cachedMessage.sentenceMessage(MessageStatus.EXECUTING_ERROR, e.getMessage());

            if(!logger.loggerGate().check(GateKey.MESSAGE_PARSER_TRASH)) return;

            logger.error("There was an issue handling the message. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake());
        }
    }
}
