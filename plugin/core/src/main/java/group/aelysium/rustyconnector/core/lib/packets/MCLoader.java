package group.aelysium.rustyconnector.core.lib.packets;

import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.Map;
import java.util.UUID;

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
