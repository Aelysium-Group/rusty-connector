package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.JsonObject;

import java.net.InetSocketAddress;

public interface IPacket {
    /**
     * Gets the protocol version this message was sent with.
     * @return {@link Integer}
     */
    int messageVersion();

    /**
     * Check if this message is sendable.
     * If not, it can be considered "received"
     * @return {@link Boolean}
     */
    boolean sendable();

    /**
     * Gets the raw message that was parsed into this instance of {@link IPacket}.
     * @return {@link String}
     */
    String rawMessage();

    /**
     * Gets the address that this message was sent with.
     * @return {@link InetSocketAddress}
     */
    InetSocketAddress address();

    /**
     * Gets the type of message this is.
     * @return {@link PacketType.Mapping}
     */
    PacketType.Mapping type();

    /**
     * Gets the origin of this packet.
     * @return {@link PacketOrigin}
     */
    PacketOrigin origin();

    JsonObject toJSON();
}
