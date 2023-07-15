package group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPingResponse;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static group.aelysium.rustyconnector.plugin.paper.central.Processor.ValidServices.MAGIC_LINK_SERVICE;

public class MagicLink_PingResponseHandler implements MessageHandler {
    private final RedisMessageServerPingResponse message;

    public MagicLink_PingResponseHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerPingResponse) message;
    }

    @Override
    public void execute() throws Exception {
        PaperAPI api = PaperRustyConnector.getAPI();
        PluginLogger logger = api.getLogger();
        MagicLinkService service = api.getService(MAGIC_LINK_SERVICE).orElseThrow();

        if(message.getStatus() == RedisMessageServerPingResponse.PingResponseStatus.ACCEPTED) {
            logger.send(Component.text(message.getMessage(), message.getColor()));

            if(message.getPingInterval().isPresent()) {
                service.setUpcomingPingDelay(message.getPingInterval().get());
            } else {
                logger.send(Component.text("No ping interval was given during registration! Defaulting to 15 seconds!", NamedTextColor.YELLOW));
                service.setUpcomingPingDelay(15);
            }

            service.setStatus(MagicLinkService.Status.CONNECTED);
        }

        if(message.getStatus() == RedisMessageServerPingResponse.PingResponseStatus.DENIED) {
            logger.send(Component.text(message.getMessage(), message.getColor()));
            logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
            service.setUpcomingPingDelay(60);
            service.setStatus(MagicLinkService.Status.SEARCHING);
        }
    }
}
