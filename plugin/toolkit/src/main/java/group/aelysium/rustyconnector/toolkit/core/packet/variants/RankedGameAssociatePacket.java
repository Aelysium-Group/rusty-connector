package group.aelysium.rustyconnector.toolkit.core.packet.variants;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RankedGameAssociatePacket extends GenericPacket {
    public UUID uuid() {
        return UUID.fromString(this.parameters.get(ValidParameters.GAME_UUID).getAsString());
    }

    private RankedGameAssociatePacket() { super(); }

    public interface ValidParameters {
        String GAME_UUID = "id";

        static List<String> toList() {
            List<String> list = new ArrayList<>();
            list.add(GAME_UUID);

            return list;
        }
    }
}
