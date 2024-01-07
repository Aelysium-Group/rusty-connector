package group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HandshakeFailurePacket extends GenericPacket {
    public String reason() {
        return this.parameters.get(Parameters.REASON).getAsString();
    }

    protected HandshakeFailurePacket(Integer messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, identification, sender, target, parameters);
    }

    public interface Parameters {
        String REASON = "r";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(REASON);

            return list;
        }
    }
}
