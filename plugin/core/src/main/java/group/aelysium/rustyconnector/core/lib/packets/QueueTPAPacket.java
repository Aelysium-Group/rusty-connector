package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class QueueTPAPacket extends GenericPacket {
    public String targetUsername() {
        return this.parameters.get(Parameters.TARGET_USERNAME).getAsString();
    }

    public String sourceUsername() {
        return this.parameters.get(Parameters.SOURCE_USERNAME).getAsString();
    }

    public QueueTPAPacket(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, PacketIdentification.Predefined.QUEUE_TPA, sender, target, parameters);
    }

    public interface Parameters {
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
