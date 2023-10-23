package group.aelysium.rustyconnector.core.plugin.lib.magic_link.handlers;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.core.lib.packets.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.core.plugin.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MagicLink_PingResponseHandler extends PacketHandler {
    @Override
    public void execute(GenericPacket genericPacket) {
        ServerPingResponsePacket packet = (ServerPingResponsePacket) genericPacket;

        Tinder api = Plugin.getAPI();
        PluginLogger logger = api.logger();
        MagicLinkService service = api.services().magicLink();

        if(packet.status() == ServerPingResponsePacket.PingResponseStatus.ACCEPTED) {
            logger.send(Component.text(packet.message(), packet.color()));

            if(packet.pingInterval().isPresent()) {
                service.setUpcomingPingDelay(packet.pingInterval().get());
            } else {
                logger.send(Component.text("No ping interval was given during registration! Defaulting to 15 seconds!", NamedTextColor.YELLOW));
                service.setUpcomingPingDelay(15);
            }

            service.setStatus(MagicLinkService.Status.CONNECTED);
        }

        if(packet.status() == ServerPingResponsePacket.PingResponseStatus.DENIED) {
            logger.send(Component.text(packet.message(), packet.color()));
            logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
            service.setUpcomingPingDelay(60);
            service.setStatus(MagicLinkService.Status.SEARCHING);
        }
    }
}
