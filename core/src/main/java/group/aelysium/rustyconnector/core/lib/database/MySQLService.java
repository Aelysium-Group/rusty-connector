package group.aelysium.rustyconnector.core.lib.database;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import group.aelysium.rustyconnector.core.lib.model.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MySQLService extends Service {
    private DataSource dataSource;
    private Connection connection;

    private MySQLService() {
        super(false);
    }
    private MySQLService(DataSource dataSource) {
        super(true);
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

    public static class MySQLBuilder {
        private boolean enabled = true;

        private String host;
        private int port;

        private String database;
        private String user;
        private String password;

        public MySQLBuilder(){}

        public MySQLBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public MySQLBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public MySQLBuilder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public MySQLBuilder setUser(String user) {
            this.user = user;
            return this;
        }

        public MySQLBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public MySQLBuilder setDisabled() {
            this.enabled = false;
            return this;
        }

        public MySQLService build(){
            if(this.enabled == false) return new MySQLService();

            MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setServerName(this.host);
            dataSource.setPortNumber(this.port);

            if(this.database != null)
                dataSource.setDatabaseName(this.database);

            if(this.user != null)
                dataSource.setUser(this.user);

            if(this.password != null)
                dataSource.setPassword(this.password);

            return new MySQLService(dataSource);
        }

    }
}