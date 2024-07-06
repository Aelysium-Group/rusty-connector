package group.aelysium.rustyconnector.common.packets;

import group.aelysium.rustyconnector.common.magic_link.Packet;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;

public interface MCLoader {
    class Lock extends Packet.Wrapper {
        public Lock(IPacket packet) {
            super(packet);
        }
    }
    class Unlock extends Packet.Wrapper {
        public Unlock(IPacket packet) {
            super(packet);
        }
    }
}
