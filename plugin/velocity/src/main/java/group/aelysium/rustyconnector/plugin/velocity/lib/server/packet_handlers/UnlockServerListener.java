package group.aelysium.rustyconnector.plugin.velocity.lib.server.packet_handlers;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;

public class UnlockServerListener extends PacketListener<group.aelysium.rustyconnector.core.lib.packets.MCLoader.Unlock> {
    protected Tinder api;

    public UnlockServerListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return BuiltInIdentifications.UNLOCK_SERVER;
    }

    @Override
    public group.aelysium.rustyconnector.core.lib.packets.MCLoader.Unlock wrap(Packet packet) {
        return new group.aelysium.rustyconnector.core.lib.packets.MCLoader.Unlock(packet);
    }

    @Override
    public void execute(group.aelysium.rustyconnector.core.lib.packets.MCLoader.Unlock packet) throws Exception {
        new MCLoader.Reference(packet.sender().uuid()).get().unlock();
    }
}
