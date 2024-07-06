package group.aelysium.rustyconnector.mcloader.magic_link.handlers;

import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLang;
import group.aelysium.rustyconnector.mcloader.magic_link.MagicLink;
import group.aelysium.lib.ranked_game_interface.RankedGameInterfaceService;
import group.aelysium.rustyconnector.toolkit.common.logger.IPluginLogger;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.packet.Packet;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.common.server.ServerAssignment;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link.ConnectedEvent;
import net.kyori.adventure.text.Component;

public class HandshakeSuccessListener extends PacketListener<group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Success> {
    public HandshakeSuccessListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING,
                new Wrapper<>() {
                    @Override
                    public group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Success wrap(IPacket packet) {
                        return new group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Success(packet);
                    }
                }
        );
    }

    @Override
    public void execute(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Success packet) {
        IPluginLogger logger = api.logger();
        MagicLink service = api.services().magicLink();
        api.services().events().fireEvent(new ConnectedEvent(packet.assignment()));

        logger.send(Component.text(packet.message(), packet.color()));
        logger.send(MCLoaderLang.MAGIC_LINK.build());

        service.setDelay(packet.pingInterval());
        api.services().serverInfo().assignment(packet.assignment());

        if(packet.assignment() == ServerAssignment.RANKED_GAME_SERVER) api.services().add(new RankedGameInterfaceService());
    }
}
