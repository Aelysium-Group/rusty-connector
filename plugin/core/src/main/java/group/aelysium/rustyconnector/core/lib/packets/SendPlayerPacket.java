package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendPlayerPacket extends Packet.Wrapper {
    public String targetFamilyName() {
        return this.parameters().get(Parameters.TARGET_FAMILY_NAME).getAsString();
    }

    public UUID uuid() {
        return UUID.fromString(this.parameters().get(Parameters.PLAYER_UUID).getAsString());
    }

    public SendPlayerPacket(Packet packet) {
        super(packet);
    }

    public interface Parameters {
        String TARGET_FAMILY_NAME = "f";
        String PLAYER_UUID = "p";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(TARGET_FAMILY_NAME);
            list.add(PLAYER_UUID);

            return list;
        }
    }
}
