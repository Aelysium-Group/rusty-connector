package group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.Map;
import java.util.UUID;

public class HandshakeKillPacket extends GenericPacket {
    protected HandshakeKillPacket(Integer messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, identification, sender, target, parameters);
    }
}
