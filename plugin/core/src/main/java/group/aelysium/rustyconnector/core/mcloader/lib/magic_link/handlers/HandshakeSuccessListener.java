package group.aelysium.rustyconnector.core.mcloader.lib.magic_link.handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.MagicLink;
import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import net.kyori.adventure.text.Component;

public class HandshakeSuccessListener extends PacketListener<MagicLink.Handshake.Success> {
    protected MCLoaderTinder api;

    public HandshakeSuccessListener(MCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.MAGICLINK_HANDSHAKE_SUCCESS;
    }

    @Override
    public MagicLink.Handshake.Success wrap(Packet packet) {
        return new MagicLink.Handshake.Success(packet);
    }

    @Override
    public void execute(MagicLink.Handshake.Success packet) {
        PluginLogger logger = api.logger();
        MagicLinkService service = (MagicLinkService) api.services().magicLink();

        logger.send(Component.text(packet.message(), packet.color()));

        service.setUpcomingPingDelay(packet.pingInterval());
        api.services().serverInfo().assignment(packet.assignment());

        service.setStatus(MagicLinkStatus.CONNECTED);
    }
}
