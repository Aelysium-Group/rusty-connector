package group.aelysium.rustyconnector.core.lib.generic;

import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.generic.hash.Snowflake;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessageCache {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private int max = 25;

    public MessageCache(Integer max) {
        this.max = max;
    }

    protected final LinkedHashMap<Long, String> messages = new LinkedHashMap<>(this.max){
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > max;
        }
    };

    /**
     * Caches a redis message, so it can be accessed later.
     * @param message The message to cache.
     * @return The id of the cached message, so it can be referenced later.
     */
    public Long cacheMessage(String message) {
        Long snowflake = this.newSnowflake();
        this.messages.put(snowflake,message);
        return snowflake;
    }

    /**
     * Gets a cached message.
     * @param messageSnowflake The snowflake of the cached message.
     * @return The cached message.
     * @throws NullPointerException If the message can't be found or has been pushed out of the cache.
     */
    public String getMessage(Long messageSnowflake) throws NullPointerException {
        return this.messages.get(messageSnowflake);
    }

    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }
}
