package rustyconnector.generic.lib;

import rustyconnector.RustyConnector;
import rustyconnector.generic.lib.database.RedisMessage;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MessageCache {
    private final LinkedHashMap<Long, RedisMessage> messages = new LinkedHashMap<>(50){
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > 50;
        }
    };;

    /**
     * Caches a redis message, so it can be accessed later.
     * @param message The message to cache.
     * @return The id of the cached message, so it can be referenced later.
     */
    public Long cacheMessage(RedisMessage message) {
        boolean isFull = this.messages.size() >= 50;
        if(isFull) this.messages.removeEldestEntry();

        Long snowflake = RustyConnector.getInstance().newSnowflake();
        this.messages.put(snowflake,message);
        return snowflake;
    }

    /**
     * Gets a cached message.
     * @param messageSnowflake The snowflake of the cached message.
     * @return The cached message.
     * @throws NullPointerException If the message can't be found or has been pushed out of the cache.
     */
    public RedisMessage getMessage(Long messageSnowflake) throws NullPointerException {
        return this.messages.get(messageSnowflake);
    }
}
