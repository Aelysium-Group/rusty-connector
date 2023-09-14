package group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnection;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageQuery;

import javax.sql.DataSource;
import java.sql.*;

public class MySQLConnection extends StorageConnection {
    private DataSource dataSource;

    protected MySQLConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Opens the connection so a query can be sent.
     */
    protected Connection open() throws SQLException {
        Connection connection = this.dataSource.getConnection();
        if (connection.isValid(1000))
            return connection;
        else
            throw new SQLException("The MySQL connection is invalid! No further information.");
    }

    @Override
    public MySQLStorageResponse query(StorageQuery query, Object ...inputs) {
        try (Connection connection = this.open()) {
            try (PreparedStatement statement = connection.prepareStatement(query.mysql())) {

                int i = 1;
                for (Object input : inputs) {
                    if (input instanceof String) statement.setString(i, (String) input);
                    else if (input instanceof Boolean) statement.setBoolean(i, (Boolean) input);
                    else if (input instanceof Integer) statement.setInt(i, (Integer) input);
                    else if (input instanceof Long) statement.setLong(i, (Long) input);
                    else if (input instanceof Double) statement.setDouble(i, (Double) input);
                    else if (input == null) statement.setNull(i, Types.NULL);

                    i++;
                }

                try (ResultSet result = statement.executeQuery();) {
                    return new MySQLStorageResponse(true, result.getFetchSize(), result);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void kill() {
        this.dataSource = null;
    }
}
