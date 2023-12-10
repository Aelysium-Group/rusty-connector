package group.aelysium.rustyconnector.core.mcloader.lib.magic_link.handlers;

import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketType;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.ServerPingResponsePacket;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MagicLink_PingResponseListener implements PacketListener {
    protected MCLoaderTinder api;

    public MagicLink_PingResponseListener(MCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketType.Mapping identifier() {
        return PacketType.PING_RESPONSE;
    }

    @Override
    public <TPacket extends IPacket> void execute(TPacket genericPacket) {
        ServerPingResponsePacket packet = (ServerPingResponsePacket) genericPacket;

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
