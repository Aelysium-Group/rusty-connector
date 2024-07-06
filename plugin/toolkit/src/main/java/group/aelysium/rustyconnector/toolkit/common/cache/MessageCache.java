package group.aelysium.rustyconnector.toolkit.common.cache;

import group.aelysium.rustyconnector.toolkit.common.crypt.Snowflake;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketStatus;
import group.aelysium.rustyconnector.toolkit.common.magic_link.packet.PacketIdentification;

import java.util.*;

public class MessageCache {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private final List<PacketStatus> ignoredStatuses;
    private final List<PacketIdentification> ignoredTypes;
    protected final int max;
    protected final Map<Long, CacheableMessage> messages;

    public MessageCache(int max, List<PacketStatus> ignoredStatuses, List<PacketIdentification> ignoredTypes) {
        if(max <= 0) max = 0;
        if(max > 500) max = 500;

        this.max = max;
        this.ignoredStatuses = ignoredStatuses;
        this.ignoredTypes = ignoredTypes;

        int finalMax = max;
        this.messages = new LinkedHashMap<>(this.max){
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > finalMax;
            }
        };
    }
    public MessageCache(int max) {
        this(max, List.of(), List.of());
    }
    public MessageCache() {
        this(50);
    }

    /**
     * Caches a redis message, so it can be accessed later.
     * @param message The message to cache.
     * @return The cached message.
     */
    public CacheableMessage cacheMessage(String message, PacketStatus status) {
        Long snowflake = this.newSnowflake();

        CacheableMessage cacheableMessage = new CacheableMessage(snowflake, message, status);

        if(this.ignoredStatuses.contains(status)) return cacheableMessage;

        this.messages.put(snowflake,cacheableMessage);

        return cacheableMessage;
    }

    public boolean ignoredType(IPacket message) {
        return this.ignoredTypes.contains(message.identification());
    }

    /**
     * Gets a cached message.
     * @param messageSnowflake The snowflake of the cached message.
     * @return The cached message.
     * @throws NullPointerException If the message can't be found or has been pushed out of the cache.
     */
    public CacheableMessage findMessage(Long messageSnowflake) throws NullPointerException {
        return this.messages.get(messageSnowflake);
    }

    /**
     * Removes a message from the cache.
     * @param messageSnowflake The snowflake of the cached message.
     */
    public void removeMessage(Long messageSnowflake) {
        this.messages.remove(messageSnowflake);
    }

    /**
     * Get all currently cached messages.
     * @return All currently cached messages.
     */
    public List<CacheableMessage> messages() {
        return this.messages.values().stream().toList();
    }

    /**
     * Get a page view of all currently cached messages.
     * @param pageNumber The page number to look at. Pages are split by 10. Page numbers start at 1 and go up.
     * @return A list of all cached messages inside a page.
     */
    public List<CacheableMessage> fetchMessagesPage(int pageNumber) {
        if(pageNumber < 1) pageNumber = 1;

        pageNumber--;

        int lowerIndex = (10 * pageNumber);
        int upperIndex = lowerIndex + 10;

        if(upperIndex > this.size()) upperIndex = this.size();

        List<CacheableMessage> messages = new ArrayList<>(this.messages());

        Collections.reverse(messages);

        return messages.subList(lowerIndex,upperIndex);
    }

    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }

    public int size() { return this.messages.size(); }

    public void empty() { this.messages.clear(); }

    public void kill() {
        this.messages.clear();
    }
}
