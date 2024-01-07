package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UnlockServerPacket extends GenericPacket {
    protected UnlockServerPacket(Integer messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, identification, sender, target, parameters);
    }
}
