package group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HandshakeFailurePacket extends GenericPacket {
    public String reason() {
        return this.parameters.get(Parameters.REASON).getAsString();
    }

    private HandshakeFailurePacket() { super(); }

    public interface Parameters {
        String REASON = "r";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(REASON);

            return list;
        }
    }
}
