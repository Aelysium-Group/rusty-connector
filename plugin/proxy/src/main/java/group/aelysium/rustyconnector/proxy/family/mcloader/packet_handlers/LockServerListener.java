package group.aelysium.rustyconnector.proxy.family.mcloader.packet_handlers;

import group.aelysium.rustyconnector.common.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.common.packets.MCLoader;
import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketListener;

public class LockServerListener extends PacketListener<MCLoader.Lock> {
    public LockServerListener() {
        super(
                BuiltInIdentifications.MAGICLINK_HANDSHAKE_PING,
                new Wrapper<>() {
                    @Override
                    public MCLoader.Lock wrap(IPacket packet) {
                        return new MCLoader.Lock(packet);
                    }
                }
        );
    }

    @Override
    public void execute(group.aelysium.rustyconnector.common.packets.MCLoader.Lock packet) throws Exception {
        RC.P.MCLoader(packet.sender().uuid()).orElseThrow().lock();
    }
}
