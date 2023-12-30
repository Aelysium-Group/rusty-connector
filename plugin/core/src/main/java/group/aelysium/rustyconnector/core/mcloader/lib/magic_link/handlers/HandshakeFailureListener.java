package group.aelysium.rustyconnector.core.mcloader.lib.magic_link.handlers;

import group.aelysium.rustyconnector.core.mcloader.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeFailurePacket;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import group.aelysium.rustyconnector.toolkit.mc_loader.magic_link.MagicLinkStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HandshakeFailureListener extends PacketListener<HandshakeFailurePacket> {
    protected IMCLoaderTinder api;

    public HandshakeFailureListener(IMCLoaderTinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_FAIL;
    }

    @Override
    public void execute(HandshakeFailurePacket packet) {
        PluginLogger logger = api.logger();
        MagicLinkService service = (MagicLinkService) api.services().magicLink();

        logger.send(Component.text(packet.reason(), NamedTextColor.RED));
        logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
        service.setUpcomingPingDelay(60);
        service.setStatus(MagicLinkStatus.SEARCHING);
    }
}
