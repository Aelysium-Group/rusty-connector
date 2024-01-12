package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.*;

public interface RankedGame {
    class Ready extends Packet.Wrapper {

        public Ready(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String ADDRESS = "a";
        }
    }

    class End extends Packet.Wrapper {
        public String reason() {
            return this.parameters().get(Parameters.REASON).getAsString();
        }

        public End(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String REASON = "r";
        }
    }

    class Imploded extends Packet.Wrapper {
        public String reason() {
            return this.parameters().get(Parameters.REASON).getAsString();
        }
        public UUID sessionUUID() {
            return UUID.fromString(this.parameters().get(Parameters.SESSION_UUID).getAsString());
        }

        public Imploded(Packet packet) {
            super(packet);
        }

        public interface Parameters {
            String REASON = "r";
            String SESSION_UUID = "u";
        }
    }
}
