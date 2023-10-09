package group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageResponse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public class MySQLStorageResponse extends StorageResponse<ResultSet> {
    public MySQLStorageResponse(boolean success, int rows, ResultSet result) {
        super(success, rows, result);
    }

    @Override
    public Type type() {
        return Type.MYSQL;
    }

    @Override
    public void forEach(Consumer<Object> consumer) throws SQLException {
        this.result.first();
        while (this.result.next()) {
            consumer.accept(this.result);
        }
    }

    @Override
    public ResultSet first() throws SQLException {
        this.result.first();
        return result;
    }

    @Override
    public void close() throws Exception {
        this.result.close();
        this.rows = 0;
    }
}
