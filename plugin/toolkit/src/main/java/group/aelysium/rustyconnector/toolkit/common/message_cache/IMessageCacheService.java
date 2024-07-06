package group.aelysium.rustyconnector.toolkit.common.message_cache;

import group.aelysium.rustyconnector.toolkit.common.packet.Packet;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketStatus;
import group.aelysium.rustyconnector.toolkit.common.serviceable.interfaces.Service;

import java.util.List;

public interface IMessageCacheService<TCacheableMessage extends ICacheableMessage> extends Service {
    /**
     * Caches a message.
     * @param message The message to cache.
     * @param status The status to assign the message.
     * @return A {@link ICacheableMessage} pointing to the message just cached.
     */
    TCacheableMessage cacheMessage(String message, PacketStatus status);

    boolean ignoredType(Packet message);

    /**
     * Find a cached message based on its snowflake.
     * @param messageSnowflake The snowflake.
     * @return {@link ICacheableMessage}
     * @throws NullPointerException If no message exists with that snowflake.
     */
    TCacheableMessage findMessage(Long messageSnowflake) throws NullPointerException;

    /**
     * Remove a message from the cache based on its snowflake.
     * @param messageSnowflake The snowflake.
     */
    void removeMessage(Long messageSnowflake);

    /**
     * Gets all cached messages.
     * @return {@link List<ICacheableMessage>}
     */
    List<TCacheableMessage> messages();

    /**
     * Generate a new snowflake ID.
     * @return {@link Long}
     */
    Long newSnowflake();

    /**
     * Gets the number of messages that are currently cached.
     * @return {@link Integer}
     */
    int size();

    /**
     * Removes all messages from the cache.
     */
    void empty();
}
