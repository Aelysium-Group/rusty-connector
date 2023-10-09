package group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnection;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;

import java.io.SyncFailedException;
import java.util.NoSuchElementException;

/**
 * A resource which is synced with a remote {@link StorageConnection}.
 */
public abstract class SyncedResource {
    protected StorageConnector<?> connector;
    protected boolean synced;
    protected boolean destroyed = false;

    /**
     * Builds a synced {@link SyncedResource}.
     * @param connector The connector to sync with.
     */
    protected SyncedResource(StorageConnector<?> connector) {
        this.connector = connector;
        this.synced = true;
    }

    /**
     * Builds an unsynced {@link SyncedResource}.
     */
    protected SyncedResource() {
        this.connector = null;
        this.synced = false;
    }

    /**
     * Checks if this {@link SyncedResource} is destroyed.
     * If this evaluates to `true` it throws a {@link NoSuchElementException}.
     * @throws NoSuchElementException If this {@link SyncedResource} is destroyed.
     */
    protected void throwDestroyed() throws NoSuchElementException {
        if(this.destroyed) throw new NoSuchElementException("Attempted to operate on a synced resource which is already destroyed.");
    }

    /**
     * Checks if this {@link SyncedResource} is synced.
     * If this evaluates to `false` it throws a {@link SyncFailedException}.
     * @throws NoSuchElementException If this {@link SyncedResource} is destroyed.
     */
    protected void throwUnsynced() throws SyncFailedException {
        if(!this.synced) throw new SyncFailedException("Attempted to operate on a synced resource which is already destroyed.");
    }

    /**
     * Deletes the {@link SyncedResource} and removes it from it's remote {@link StorageConnection}.
     */
    @Destructive
    public void delete() throws Exception {}

    /**
     * Syncs this resource with the remote resource.
     * The sync operates via a local push to the remote resource.
     * If the remote resource already has this item, it will update to what the {@link SyncedResource} already has.
     */
    public void sync(StorageConnector<?> connector) throws Exception {}
}
