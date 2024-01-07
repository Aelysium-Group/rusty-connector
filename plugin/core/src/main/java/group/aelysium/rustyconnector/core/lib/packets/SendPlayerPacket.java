package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SendPlayerPacket extends GenericPacket {
    public String targetFamilyName() {
        return this.parameters.get(Parameters.TARGET_FAMILY_NAME).getAsString();
    }

    public UUID uuid() {
        return UUID.fromString(this.parameters.get(Parameters.PLAYER_UUID).getAsString());
    }

    public SendPlayerPacket(Integer messageVersion, UUID sender, UUID target, Map<String, PacketParameter> parameters) {
        super(messageVersion, PacketIdentification.Predefined.SEND_PLAYER, sender, target, parameters);
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
