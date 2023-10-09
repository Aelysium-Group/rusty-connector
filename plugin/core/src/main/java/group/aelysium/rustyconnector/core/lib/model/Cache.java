package group.aelysium.rustyconnector.core.lib.model;

import group.aelysium.rustyconnector.core.lib.hash.Snowflake;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cache<D> extends Service {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private int maxCapacity = 25;

    protected final LinkedHashMap<Long, D> items = new LinkedHashMap<>(this.maxCapacity){
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > maxCapacity;
        }
    };

    public Cache(Integer maxCapacity) {
        if(maxCapacity <= 0) maxCapacity = 0;
        if(maxCapacity > 500) maxCapacity = 500;

        this.maxCapacity = maxCapacity;
    }


    public synchronized Long put(D item) {
        Long snowflake = this.newSnowflake();
        this.items.put(snowflake,item);
        return snowflake;
    }

    /**
     * Gets a cached item.
     * @param messageSnowflake The snowflake of the cached item.
     * @return The cached item.
     * @throws NullPointerException If the item can't be found or has been pushed out of the cache.
     */
    public synchronized D get(Long messageSnowflake) throws NullPointerException {
        return this.items.get(messageSnowflake);
    }

    /**
     * Removes an item from the cache.
     * @param messageSnowflake The snowflake of the cached item.
     */
    public synchronized void remove(Long messageSnowflake) {
        this.items.remove(messageSnowflake);
    }

    /**
     * Get all currently cached items.
     * @return All currently cached items.
     */
    public List<D> getAll() {
        return this.items.values().stream().toList();
    }

    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }

    public int getSize() { return this.items.size(); }

    public void empty() { this.items.clear(); }

    @Override
    public void kill() {
        this.items.clear();
    }
}
