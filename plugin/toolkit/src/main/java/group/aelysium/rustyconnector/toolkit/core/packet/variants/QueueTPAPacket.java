package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;

import java.util.ArrayList;
import java.util.List;

public class QueueTPAPacket extends GenericPacket {
    public String targetUsername() {
        return this.parameters.get(ValidParameters.TARGET_USERNAME).getAsString();
    }

    public String sourceUsername() {
        return this.parameters.get(ValidParameters.SOURCE_USERNAME).getAsString();
    }

    private QueueTPAPacket() { super(); }

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
