package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.Map;
import java.util.UUID;

public class LockServerPacket extends GenericPacket {
    protected LockServerPacket(Integer messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, identification, sender, target, parameters);
    }
}
