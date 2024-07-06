package group.aelysium.rustyconnector.mcloader.magic_link.handlers;

import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.mcloader.Flame;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketParameter;
import group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link.TimeoutEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

public class HandshakeStalePingListener extends PacketListener<group.aelysium.rustyconnector.common.packets.MagicLink.StalePing> {
    public HandshakeStalePingListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_STALE_PING,
                new Wrapper<>() {
                    @Override
                    public group.aelysium.rustyconnector.common.packets.MagicLink.StalePing wrap(IPacket packet) {
                        return new group.aelysium.rustyconnector.common.packets.MagicLink.StalePing(packet);
                    }
                }
        );
    }

    @Override
    public void execute(group.aelysium.rustyconnector.common.packets.MagicLink.StalePing packet) {
        Flame flame = RustyConnector.Toolkit.MCLoader().orElseThrow().access().get(20, TimeUnit.SECONDS);
        RC.M.EventManager().fireEvent(new TimeoutEvent());

        logger.send(Component.text("Connection to the Proxy has timed out! Attempting to reconnect...", NamedTextColor.RED));
        RC.M.MagicLink().setDelay(5);
        Packet.New()
                .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING)
                .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.ADDRESS, flame.address().getHostName() + ":" + flame.address().getPort())
                .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.DISPLAY_NAME, flame.displayName())
                .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.MAGIC_CONFIG_NAME, flame.MagicLink().orElseThrow().magicConfig())
                .parameter(group.aelysium.rustyconnector.common.packets.MagicLink.Handshake.Ping.Parameters.PLAYER_COUNT, new PacketParameter(flame.playerCount()))
                .addressedTo(packet)
                .send();
    }
}
