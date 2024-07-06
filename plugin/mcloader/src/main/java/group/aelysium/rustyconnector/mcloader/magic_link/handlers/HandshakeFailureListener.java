package group.aelysium.rustyconnector.mcloader.magic_link.handlers;

import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.mcloader.magic_link.MagicLink;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.logger.IPluginLogger;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class HandshakeFailureListener extends PacketListener<group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Failure> {
    public HandshakeFailureListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_FAIL,
                new Wrapper<>() {
                    @Override
                    public group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Failure wrap(IPacket packet) {
                        return new group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Failure(packet);
                    }
                }
        );
    }

    @Override
    public void execute(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Failure packet) {
        logger.send(Component.text(packet.reason(), NamedTextColor.RED));
        logger.send(Component.text("Waiting 1 minute before trying again...", NamedTextColor.GRAY));
        RC.M.MagicLink().setDelay(60);
    }
}
