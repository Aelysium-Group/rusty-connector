package group.aelysium.rustyconnector.toolkit.core.packet;

import com.google.gson.JsonObject;

import java.util.UUID;

public interface IPacket {
    /**
     * Gets the protocol version this message was sent with.
     * @return {@link Integer}
     */
    int messageVersion();

    /**
     * Gets the UUID that this message was sent from.
     * @return {@link UUID}
     */
    UUID sender();

    /**
     * Gets the UUID that this packet was sent to.
     * @return {@link UUID}
     */
    UUID target();

    /**
     * Gets the type of message this is.
     * @return {@link PacketIdentification}
     */
    PacketIdentification identification();

    JsonObject toJSON();
}
