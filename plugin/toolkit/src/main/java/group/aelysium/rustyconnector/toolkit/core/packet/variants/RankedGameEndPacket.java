package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankedGameEndPacket extends GenericPacket {
    public String familyName() {
        return this.parameters.get(ValidParameters.FAMILY_NAME).getAsString();
    }
    public String serverName() {
        return this.parameters.get(ValidParameters.SERVER_NAME).getAsString();
    }
    public UUID uuid() {
        return UUID.fromString(this.parameters.get(ValidParameters.GAME_UUID).getAsString());
    }

    private RankedGameEndPacket() { super(); }

    public interface ValidParameters {
        String FAMILY_NAME = "f";
        String SERVER_NAME = "sn";
        String GAME_UUID = "id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(FAMILY_NAME);
            list.add(SERVER_NAME);
            list.add(GAME_UUID);

            return list;
        }
    }
}
