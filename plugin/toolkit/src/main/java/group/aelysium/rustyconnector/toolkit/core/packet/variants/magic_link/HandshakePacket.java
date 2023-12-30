package group.aelysium.rustyconnector.toolkit.core.packet.variants.magic_link;

import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketParameter;

import java.util.Optional;
import java.util.UUID;

public class HandshakePacket extends GenericPacket {
    public String address() {
        return this.parameters.get(Parameters.ADDRESS).getAsString();
    }
    public Optional<String> displayName() {
        String displayName = this.parameters.get(Parameters.DISPLAY_NAME).getAsString();
        if(displayName.isEmpty()) return Optional.empty();
        return Optional.of(displayName);
    }
    public String magicConfigName() {
        return this.parameters.get(Parameters.MAGIC_CONFIG_NAME).getAsString();
    }
    public Integer playerCount() {
        return this.parameters.get(Parameters.PLAYER_COUNT).getAsInt();
    }
    public String podName() {
        return this.parameters.get(Parameters.POD_NAME).getAsString();
    }

    private HandshakePacket() { super(); }

    public interface Parameters {
        String ADDRESS = "a";
        String DISPLAY_NAME = "n";
        String MAGIC_CONFIG_NAME = "c";
        String PLAYER_COUNT = "pc";
        String POD_NAME = "k8";
    }

    public static HandshakePacket create(UUID uuid, String address, String displayName, String magicConfigName, int playerCount) {
        return new GenericPacket.Builder()
                .identification(PacketIdentification.Predefined.MAGICLINK_HANDSHAKE)
                .toProxy(uuid)
                .parameter(Parameters.ADDRESS, address)
                .parameter(Parameters.DISPLAY_NAME, displayName)
                .parameter(Parameters.MAGIC_CONFIG_NAME, magicConfigName)
                .parameter(Parameters.PLAYER_COUNT, new PacketParameter(playerCount))
                .build();
    }
}
