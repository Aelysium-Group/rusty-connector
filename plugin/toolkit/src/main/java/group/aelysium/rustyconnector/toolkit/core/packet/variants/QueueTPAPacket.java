package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QueueTPAPacket extends GenericPacket {
    public String targetUsername() {
        return this.parameters.get(ValidParameters.TARGET_USERNAME).getAsString();
    }

    public String sourceUsername() {
        return this.parameters.get(ValidParameters.SOURCE_USERNAME).getAsString();
    }

    protected QueueTPAPacket(Integer messageVersion, PacketIdentification identification, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, identification, sender, target, parameters);
    }

    public interface ValidParameters {
        String TARGET_USERNAME = "tp";
        String SOURCE_USERNAME = "sp";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(TARGET_USERNAME);
            list.add(SOURCE_USERNAME);

            return list;
        }
    }
}
