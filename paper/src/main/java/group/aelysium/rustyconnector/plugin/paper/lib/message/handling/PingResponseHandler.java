package group.aelysium.rustyconnector.plugin.paper.lib.message.handling;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPingResponse;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging.PaperLang;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ProxyConnectorService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class PingResponseHandler implements MessageHandler {
    private final RedisMessageServerPingResponse message;

    public PingResponseHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerPingResponse) message;
    }

    @Override
    public void execute() throws Exception {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        ProxyConnectorService service = api.getService(ProxyConnectorService.class);

        if(message.getStatus() == RedisMessageServerPingResponse.PingResponseStatus.ACCEPTED) {
            logger.send(Component.text(message.getMessage(), message.getColor()));

            if(message.getPingInterval().isPresent()) {
                service.setUpcomingPingDelay(message.getPingInterval().get());
                service.setNextcomingPingDelay(message.getPingInterval().get());
            } else {
                service.setUpcomingPingDelay(10);
                service.setNextcomingPingDelay(10);
            }

            service.setStatus(ProxyConnectorService.Status.CONNECTED);
        }

        if(message.getStatus() == RedisMessageServerPingResponse.PingResponseStatus.DENIED) {
            logger.send(Component.text(message.getMessage(), message.getColor()));
            logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
            service.setUpcomingPingDelay(60);
            service.setNextcomingPingDelay(10);
            service.setStatus(ProxyConnectorService.Status.SEARCHING);
        }
    }
}
