package group.aelysium.rustyconnector.plugin.paper.lib.magic_link.handlers;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageHandler;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.variants.RedisMessageServerPingResponse;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MagicLink_PingResponseHandler implements MessageHandler {
    private final RedisMessageServerPingResponse message;

    public MagicLink_PingResponseHandler(GenericRedisMessage message) {
        this.message = (RedisMessageServerPingResponse) message;
    }

    @Override
    public void execute() throws Exception {
        PaperAPI api = PaperAPI.get();
        PluginLogger logger = api.logger();
        MagicLinkService service = api.services().magicLinkService();

        if(message.status() == RedisMessageServerPingResponse.PingResponseStatus.ACCEPTED) {
            logger.send(Component.text(message.message(), message.color()));

            if(message.pingInterval().isPresent()) {
                service.setUpcomingPingDelay(message.pingInterval().get());
            } else {
                logger.send(Component.text("No ping interval was given during registration! Defaulting to 15 seconds!", NamedTextColor.YELLOW));
                service.setUpcomingPingDelay(15);
            }

            service.setStatus(MagicLinkService.Status.CONNECTED);
        }

        if(message.status() == RedisMessageServerPingResponse.PingResponseStatus.DENIED) {
            logger.send(Component.text(message.message(), message.color()));
            logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
            service.setUpcomingPingDelay(60);
            service.setStatus(MagicLinkService.Status.SEARCHING);
        }
    }
}
