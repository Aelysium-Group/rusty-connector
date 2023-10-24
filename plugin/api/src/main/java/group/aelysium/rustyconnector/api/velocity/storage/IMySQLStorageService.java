package group.aelysium.rustyconnector.api.velocity.storage;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

/**
 * {@link IMySQLStorageService} implements MicroStream storage connector architecture to dynamically make database requests as you look for data via the RustyConnector Java API.
 * Simply fetch {@link IMySQLStorageService#root()} and use any of the available methods to fetch the data you need!
 * To store new data to the database, use {@link IMySQLStorageService#store(Object)}.
 */
public interface IMySQLStorageService extends Service {
    /**
     * Gets the {@link IStorageRoot}. With it, you can access the entire RustyConnector database dataset.
     * Read the {@link IMySQLStorageService} Javadoc for more details.
     * @return {@link IStorageRoot}
     */
    IStorageRoot root();

    /**
     * Convenience method to store the passed object synchronously.
     * Running this is equivalent to running
     * <pre>
     * XThreads.executeSynchronized(() -> {
     *      storageManager.store(object);
     * });
     * </pre>
     * Objects should be stored in accordance with <a href="https://docs.microstream.one/manual/storage/storing-data/index.html">the MicroStream docs</a>.
     * If you create a new object, you store the parent of that object.
     * If you modify and object, you store the object itself.
     * @param object The object to store. You should store objects based on <a href="https://docs.microstream.one/manual/storage/storing-data/index.html">"The Object that has been modified has to be stored."</a>
     */
    void store(Object object);
}
