package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.UnlockServerPacket;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

public class UnlockServerListener extends PacketListener<UnlockServerPacket> {
    protected Tinder api;

    public UnlockServerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.UNLOCK_SERVER;
    }

    @Override
    public void execute(UnlockServerPacket packet) throws Exception {
        new MCLoader.Reference(packet.sender()).get().unlock();
    }
}
