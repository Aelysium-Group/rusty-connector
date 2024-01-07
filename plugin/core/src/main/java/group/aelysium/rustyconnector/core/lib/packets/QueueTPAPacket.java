package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.toolkit.core.packet.Packet;

public class QueueTPAPacket extends Packet.Wrapper {
    public QueueTPAPacket(Packet packet) {
        super(packet);
    }

    public String targetUsername() {
        return this.parameters().get(Parameters.TARGET_USERNAME).getAsString();
    }

    public String sourceUsername() {
        return this.parameters().get(Parameters.SOURCE_USERNAME).getAsString();
    }

    public interface Parameters {
        String TARGET_USERNAME = "tp";
        String SOURCE_USERNAME = "sp";
    }
}
