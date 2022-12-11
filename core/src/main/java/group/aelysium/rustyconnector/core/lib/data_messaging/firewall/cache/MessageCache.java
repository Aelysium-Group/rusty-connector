package group.aelysium.rustyconnector.core.lib.data_messaging.firewall.cache;

import group.aelysium.rustyconnector.core.lib.data_messaging.MessageStatus;
import group.aelysium.rustyconnector.core.lib.hash.Snowflake;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageCache {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private int max = 25;

    public MessageCache(Integer max) {
        this.max = max;
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

        this.messages.put(snowflake,cacheableMessage);
        return cacheableMessage;
    }

    /**
     * Gets a cached message.
     * @param messageSnowflake The snowflake of the cached message.
     * @return The cached message.
     * @throws NullPointerException If the message can't be found or has been pushed out of the cache.
     */
    public CacheableMessage getMessage(Long messageSnowflake) throws NullPointerException {
        return this.messages.get(messageSnowflake);
    }

    /**
     * Get all currently cached messages.
     * @return All currently cached messages.
     */
    public List<CacheableMessage> getMessages() {
        return this.messages.values().stream().toList();
    }

    /**
     * Get a page view of all currently cached messages.
     * @param pageNumber The page number to look at. Pages are split by 10. Page numbers start at 1 and go up.
     * @return A list of all cached messages inside of a page.
     */
    public List<CacheableMessage> getMessagesPage(int pageNumber) {
        if(pageNumber < 1) pageNumber = 1;

        pageNumber--;

        int lowerIndex = (10 * pageNumber);
        int upperIndex = lowerIndex + 10;

        if(upperIndex > this.getSize()) upperIndex = this.getSize();

        List<CacheableMessage> messages = this.getMessages();

        return messages.subList(lowerIndex,upperIndex);
    }

    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }

    public int getSize() { return this.messages.size(); }
}
