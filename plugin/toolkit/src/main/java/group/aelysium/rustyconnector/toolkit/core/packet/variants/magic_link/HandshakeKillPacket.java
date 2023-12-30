package group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

import java.util.UUID;

public class HandshakeKillPacket extends GenericPacket {
    private HandshakeKillPacket() { super(); }
    public static HandshakeKillPacket create(UUID uuid) {
        return new Builder()
                .identification(PacketIdentification.Predefined.MAGICLINK_HANDSHAKE_KILL)
                .toProxy(uuid)
                .build();
    }
}
