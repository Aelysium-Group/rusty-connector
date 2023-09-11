package group.aelysium.rustyconnector.plugin.paper.lib.connectors;

import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.packets.PacketOrigin;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers.CoordinateRequestHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers.MagicLink_PingResponseHandler;

import javax.naming.AuthenticationException;

public class RedisSubscriber extends group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisSubscriber {
    public RedisSubscriber(RedisClient client) {
        super(client);
    }

    @Override
    public void onMessage(String rawMessage) {
        Tinder api = Tinder.get();
        PluginLogger logger = api.logger();

        CacheableMessage cachedMessage = api.services().messageCacheService().cacheMessage(rawMessage, PacketStatus.UNDEFINED);

        try {
            GenericPacket.Serializer serializer = new GenericPacket.Serializer();
            GenericPacket message = serializer.parseReceived(rawMessage);

            if(message.origin() == PacketOrigin.SERVER) throw new Exception("Message from a sub-server! Ignoring...");


            if(!AddressUtil.addressToString(message.address()).equals(api.services().serverInfoService().address()))
               throw new Exception("Message addressed to another sub-server! Ignoring...");
            try {
                if (!(api.services().redisService().connection().orElseThrow().validatePrivateKey(message.privateKey())))
                    throw new AuthenticationException("This message has an invalid private key!");


                cachedMessage.sentenceMessage(PacketStatus.ACCEPTED);

                RedisSubscriber.processParameters(message, cachedMessage);
            } catch (AuthenticationException e) {
                logger.error("Incoming message from: " + message.address().toString() + " contains an invalid private key! Throwing away...");
                logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
            }
        } catch (Exception e) {
            cachedMessage.sentenceMessage(PacketStatus.TRASHED);
            /* TODO: Uncomment and implement proper logging handling

            logger.error("There was an issue handling the incoming message! Throwing away...",e);
            logger.log("To view the thrown away message use: /rc message get "+cachedMessage.getSnowflake().toString());
            */
        }
    }

    private static void processParameters(GenericPacket message, CacheableMessage cachedMessage) {
        PluginLogger logger = Tinder.get().logger();

        try {
            if(message.type() == PING_RESPONSE)      new MagicLink_PingResponseHandler(message).execute();
            if(message.type() == COORDINATE_REQUEST_QUEUE)   new CoordinateRequestHandler(message).execute();

            cachedMessage.sentenceMessage(PacketStatus.EXECUTED);
        } catch (Exception e) {
            cachedMessage.sentenceMessage(PacketStatus.PARSING_ERROR);

            logger.error("Incoming message " + message.type().name() + " from " + message.address() + " is not formatted properly. Throwing away...", e);
            logger.log("To view the thrown away message use: /rc message get " + cachedMessage.getSnowflake());
        }
    }
}
