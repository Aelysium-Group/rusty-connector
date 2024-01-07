package group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.packet_handlers;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link.HandshakeKillPacket;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.Optional;

public class HandshakeKillListener extends PacketListener<HandshakeKillPacket> {
    protected Tinder api;

    public HandshakeKillListener(Tinder api) {
        this.api = api;
    }

    @Override
    public PacketIdentification target() {
        return PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_KILL;
    }

    @Override
    public void execute(HandshakeKillPacket packet) throws Exception {
        Optional<IMCLoader> mcLoader = api.services().server().fetch(packet.sender());
        mcLoader.orElseThrow().unregister(true);
    }
}
