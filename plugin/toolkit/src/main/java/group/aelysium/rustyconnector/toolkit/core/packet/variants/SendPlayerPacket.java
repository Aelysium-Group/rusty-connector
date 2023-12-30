package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SendPlayerPacket extends GenericPacket {
    public String targetFamilyName() {
        return this.parameters.get(ValidParameters.TARGET_FAMILY_NAME).getAsString();
    }

    public UUID uuid() {
        return UUID.fromString(this.parameters.get(ValidParameters.PLAYER_UUID).getAsString());
    }

    private SendPlayerPacket() { super(); }

    public interface ValidParameters {
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
