package group.aelysium.rustyconnector.core.lib.database.mysql;

import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLConnection extends StorageConnection {
    private DataSource dataSource;
    private Connection connection;

    protected MySQLConnection(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets the connection to the MySQL server.
     */
    public void connect() throws SQLException {
        Connection connection = this.dataSource.getConnection();
        if (connection.isValid(1000))
            this.connection = connection;
        else
            throw new SQLException("The MySQL connection is invalid! No further information.");
    }

    /**
     * Closes the connection to the MySQL server.
     */
    public void close() throws SQLException {
        if(this.connection == null) return;
        try {
            this.connection.commit();
        } catch (Exception ignore) {} // If autocommit=true this throws an exception. Just ignore it.
        this.connection.close();
    }

    public List<PreparedStatement> prepareMultiple(String statement) throws SQLException {
        String[] queries = statement.split(";");

        List<PreparedStatement> statements = new ArrayList<>();
        for (String query : queries) {
            if (query.replaceAll("\\s","").isEmpty()) continue;

            Connection connection = this.connection;
            if(connection == null) throw new SQLException("There is no open MySQL connection!");

            statements.add(connection.prepareStatement(query));
        }

        return statements;
    }

    public PreparedStatement prepare(String statement) throws SQLException {
        return connection.prepareStatement(statement);
    }

    public boolean execute(PreparedStatement statement) throws SQLException {
        return statement.execute();
    }

    public ResultSet executeQuery(PreparedStatement statement) throws SQLException {
        return statement.executeQuery();
    }

    public void executeMultiple(List<PreparedStatement> statements) throws SQLException {
        for (PreparedStatement statement : statements) {
            statement.execute();
        }
    }

    @Override
    public void kill() {
        try {
            this.connection.close();
        } catch (Exception ignore) {}

        this.dataSource = null;
    }
}
