package group.aelysium.rustyconnector.common.packets;

import group.aelysium.central.MCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.common.packet.Packet;

public interface MCLoader {
    class Lock extends Packet.Wrapper {
        public Lock(Packet packet) {
            super(packet);
        }

        public static Packet build(MCLoaderFlame flame) {
            return flame.services().packetBuilder().newBuilder()
                    .identification(BuiltInIdentifications.LOCK_SERVER)
                    .sendingToProxy()
                    .build();
        }
    }
    class Unlock extends Packet.Wrapper {
        public Unlock(Packet packet) {
            super(packet);
        }

        public static Packet build(MCLoaderFlame flame) {
            return flame.services().packetBuilder().newBuilder()
                    .identification(BuiltInIdentifications.UNLOCK_SERVER)
                    .sendingToProxy()
                    .build();
        }
    }
}