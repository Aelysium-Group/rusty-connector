package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UnlockServerPacket extends GenericPacket {
    public UnlockServerPacket(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, PacketIdentification.Predefined.UNLOCK_SERVER, sender, target, parameters);
    }
}
