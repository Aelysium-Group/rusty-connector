package group.aelysium.rustyconnector.core.lib.connectors.storage;

import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLStorageResponse;

import java.util.function.Consumer;

/**
 * A consumer which allows you to define different handlers depending on what type the {@link StorageResponse} is.
 */
public class ResultConsumer {
    protected Consumer<MySQLStorageResponse> mysqlConsumer = null;
    protected StorageResponse<?> response;

    public ResultConsumer(StorageResponse<?> response) {
        this.response = response;
    }

    public void forMySQL(Consumer<MySQLStorageResponse> consumer) {
        this.mysqlConsumer = consumer;
    }

    public void trigger() {
        if(this.response instanceof MySQLStorageResponse) this.mysqlConsumer.accept((MySQLStorageResponse) this.response);
        throw new NullPointerException("No consumer exists for result: "+this.response.type());
    }
}
