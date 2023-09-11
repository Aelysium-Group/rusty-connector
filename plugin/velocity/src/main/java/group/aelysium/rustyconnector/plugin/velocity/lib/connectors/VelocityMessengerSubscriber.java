package group.aelysium.rustyconnector.plugin.velocity.lib.connectors;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerSubscriber;
import group.aelysium.rustyconnector.core.lib.packets.PacketStatus;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.lang_messaging.GateKey;
import group.aelysium.rustyconnector.plugin.velocity.PluginLogger;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.handlers.MagicLinkPingHandler;
import group.aelysium.rustyconnector.plugin.velocity.lib.message.handling.*;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;

import static group.aelysium.rustyconnector.core.lib.packets.PacketType.PING;
import static group.aelysium.rustyconnector.core.lib.packets.PacketType.SEND_PLAYER;

public class VelocityMessengerSubscriber extends MessengerSubscriber {
    public VelocityMessengerSubscriber(char[] privateKey, MessageCacheService cache, PluginLogger logger) {
        super(privateKey, cache, logger);
    }

    @Override
    protected void processParameters(GenericPacket message, CacheableMessage cachedMessage) {
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
