package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.Map;
import java.util.UUID;

public class LockServerPacket extends GenericPacket {
    public LockServerPacket(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, PacketIdentification.Predefined.LOCK_SERVER, sender, target, parameters);
    }
}
