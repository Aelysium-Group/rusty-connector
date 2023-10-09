package group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that this method builds and returns a {@link SyncedResource} which is out of sync with the remote resource.
 * It can be synced by running {@link SyncedResource#sync(StorageConnector) SyncedResource.sync()}.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface Unsynced {
}
