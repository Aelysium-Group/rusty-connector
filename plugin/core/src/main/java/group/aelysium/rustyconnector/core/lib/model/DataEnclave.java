package group.aelysium.rustyconnector.core.lib.model;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

public class DataEnclave<K, V> extends Service {
    protected Cache<K, V> cache;
    protected StorageConnector<?> connector;

    /**
     * @param maxSize Max size the enclave cache can be.
     * @param expire The amount of time entries can exist for before we're removed from cache.
     */
    public DataEnclave(StorageConnector<?> connector, int maxSize, LiquidTimestamp expire) {
        this.connector = connector;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expire.value(), expire.unit())
                .build();
    }

    /**
     * @param maxSize Max size the enclave cache can be.
     */
    public DataEnclave(StorageConnector<?> connector, int maxSize) {
        this.connector = connector;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .build();
    }

    public DataEnclave(StorageConnector<?> connector) {
        this.connector = connector;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(250)
                .build();
    }

    private DataEnclave() {}

    @Override
    public void kill() {
        this.cache.invalidateAll();
        this.cache = null;
    }
}
