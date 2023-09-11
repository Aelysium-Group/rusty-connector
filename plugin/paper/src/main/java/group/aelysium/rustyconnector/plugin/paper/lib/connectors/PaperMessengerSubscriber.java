package group.aelysium.rustyconnector.plugin.paper.lib.connectors;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.handlers.CoordinateRequestHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers.MagicLink_PingResponseHandler;

import static group.aelysium.rustyconnector.core.lib.packets.PacketType.COORDINATE_REQUEST_QUEUE;
import static group.aelysium.rustyconnector.core.lib.packets.PacketType.PING_RESPONSE;

public class PaperMessengerSubscriber extends MessengerSubscriber {
    public PaperMessengerSubscriber(char[] privateKey, MessageCacheService cache, PluginLogger logger) {
        super(privateKey, cache, logger);
    }

    @Override
    protected void processParameters(GenericPacket message, CacheableMessage cachedMessage) {
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
