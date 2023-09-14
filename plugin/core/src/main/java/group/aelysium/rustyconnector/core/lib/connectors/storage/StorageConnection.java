package group.aelysium.rustyconnector.core.lib.connectors.storage;

import group.aelysium.rustyconnector.core.lib.connectors.Connection;

public abstract class StorageConnection extends Connection {
    public abstract StorageResponse<?> query(StorageQuery query, Object ...inputs) throws Exception;
}
