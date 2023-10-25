package group.aelysium.rustyconnector.core.plugin.lib.magic_link.handlers;

import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.mc_loader.magic_link.MagicLinkStatus;
import group.aelysium.rustyconnector.core.lib.packets.GenericPacket;
import group.aelysium.rustyconnector.api.core.packet.PacketHandler;
import group.aelysium.rustyconnector.core.lib.packets.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.plugin.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MagicLink_PingResponseHandler implements PacketHandler<GenericPacket> {
    @Override
    public void execute(GenericPacket genericPacket) {
        ServerPingResponsePacket packet = (ServerPingResponsePacket) genericPacket;

        MCLoaderTinder api = TinderAdapterForCore.getTinder();
        PluginLogger logger = api.logger();
        MagicLinkService service = (MagicLinkService) api.services().magicLink();

        if(packet.status() == ServerPingResponsePacket.PingResponseStatus.ACCEPTED) {
            logger.send(Component.text(packet.message(), packet.color()));

            if(packet.pingInterval().isPresent()) {
                service.setUpcomingPingDelay(packet.pingInterval().get());
            } else {
                logger.send(Component.text("No ping interval was given during registration! Defaulting to 15 seconds!", NamedTextColor.YELLOW));
                service.setUpcomingPingDelay(15);
            }

            service.setStatus(MagicLinkStatus.CONNECTED);
        }

        if(packet.status() == ServerPingResponsePacket.PingResponseStatus.DENIED) {
            logger.send(Component.text(packet.message(), packet.color()));
            logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
            service.setUpcomingPingDelay(60);
            service.setStatus(MagicLinkStatus.SEARCHING);
        }
    }
}
