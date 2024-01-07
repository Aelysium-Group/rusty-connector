package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.core.lib.packets.LockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

public class LockServerListener extends PacketListener<LockServerPacket> {
    protected Tinder api;

    public LockServerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.LOCK_SERVER;
    }
    @Override
    public void execute(LockServerPacket packet) throws Exception {
        new MCLoader.Reference(packet.sender()).get().lock();
    }
}
