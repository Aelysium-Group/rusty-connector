package group.aelysium.rustyconnector.core.lib.connectors.storage;

import group.aelysium.rustyconnector.core.lib.connectors.Connection;

import java.util.function.Consumer;

public abstract class StorageConnection<R extends StorageResponse<?>> extends Connection {
    public abstract void query(StorageQuery query, Consumer<R> callback, Object ...inputs) throws Exception;
    public abstract void query(StorageQuery query, Object ...inputs) throws Exception;
}
