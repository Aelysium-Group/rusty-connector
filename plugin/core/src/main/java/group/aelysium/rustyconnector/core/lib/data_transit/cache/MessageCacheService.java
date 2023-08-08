package group.aelysium.rustyconnector.core.lib.data_transit.cache;

import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.MessageStatus;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.RedisMessageType;
import group.aelysium.rustyconnector.core.lib.hash.Snowflake;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageCacheService extends Service {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private final List<MessageStatus> ignoredStatuses;
    private final List<RedisMessageType.Mapping> ignoredTypes;
    private int max = 25;

    public MessageCacheService(Integer max) {
        if(max <= 0) max = 0;
        if(max > 500) max = 500;

        this.max = max;
        this.ignoredStatuses = new ArrayList<>(0);
        this.ignoredTypes = new ArrayList<>(0);
    }
    public MessageCacheService(Integer max, List<MessageStatus> ignoredStatuses, List<RedisMessageType.Mapping> ignoredTypes) {
        if(max <= 0) max = 0;
        if(max > 500) max = 500;

        this.max = max;
        this.ignoredStatuses = ignoredStatuses;
        this.ignoredTypes = ignoredTypes;
    }

    protected final LinkedHashMap<Long, CacheableMessage> messages = new LinkedHashMap<>(this.max){
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > max;
        }
    };

    /**
     * Caches a redis message, so it can be accessed later.
     * @param message The message to cache.
     * @return The cached message.
     */
    public CacheableMessage cacheMessage(String message, MessageStatus status) {
        Long snowflake = this.newSnowflake();

        CacheableMessage cacheableMessage = new CacheableMessage(snowflake, message, status);

        if(this.ignoredStatuses.contains(status)) return cacheableMessage;

        this.messages.put(snowflake,cacheableMessage);

        return cacheableMessage;
    }

    public boolean ignoredType(GenericRedisMessage message) {
        return this.ignoredTypes.contains(message.type());
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
     * @return A list of all cached messages inside of a page.
     */
    public List<CacheableMessage> fetchMessagesPage(int pageNumber) {
        if(pageNumber < 1) pageNumber = 1;

        pageNumber--;

        int lowerIndex = (10 * pageNumber);
        int upperIndex = lowerIndex + 10;

        if(upperIndex > this.size()) upperIndex = this.size();

        List<CacheableMessage> messages = this.messages();

        return messages.subList(lowerIndex,upperIndex);
    }

    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }

    public int size() { return this.messages.size(); }

    public void empty() { this.messages.clear(); }

    @Override
    public void kill() {
        this.messages.clear();
    }
}
