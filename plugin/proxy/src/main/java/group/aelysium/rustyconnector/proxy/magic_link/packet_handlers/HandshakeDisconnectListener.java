package group.aelysium.rustyconnector.proxy.magic_link.packet_handlers;

import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.common.packets.MagicLink;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.proxy.events.mc_loader.MCLoaderUnregisterEvent;
import group.aelysium.rustyconnector.toolkit.proxy.family.IFamily;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.IMCLoader;

import java.util.concurrent.TimeUnit;

public class HandshakeDisconnectListener extends PacketListener<MagicLink.Disconnect> {
    public HandshakeDisconnectListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_DISCONNECT,
                new Wrapper<>() {
                    @Override
                    public MagicLink.Disconnect wrap(IPacket packet) {
                        return new MagicLink.Disconnect(packet);
                    }
                }
        );
    }

    @Override
    public void execute(MagicLink.Disconnect packet) throws Exception {
        IMCLoader mcloader = RC.P.MCLoader(packet.sender().uuid()).orElseThrow();

        IFamily family = mcloader.family().access().get(10, TimeUnit.SECONDS);

        RC.P.Adapter().unregisterMCLoader(mcloader);
        family.connector().unregister(mcloader);

        try {
            Packet.New()
                    .identification(BuiltInIdentifications.MAGICLINK_HANDSHAKE_STALE_PING)
                    .addressedTo(packet)
                    .send();
        } catch (Exception ignore) {}

        RC.P.EventManager().fireEvent(new MCLoaderUnregisterEvent(family, mcloader));
    }
}