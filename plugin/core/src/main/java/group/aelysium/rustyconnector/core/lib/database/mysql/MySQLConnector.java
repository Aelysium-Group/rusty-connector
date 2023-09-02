package group.aelysium.rustyconnector.core.lib.database.mysql;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.storage.StorageConnector;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.net.InetSocketAddress;

public class MySQLConnector extends StorageConnector<MySQLConnection> {
    protected final String database;

    private MySQLConnector(InetSocketAddress address, UserPass userPass, String database) {
        super(address, userPass);
        this.database = database;
    }

    @Override
    public MySQLConnection connect() throws ConnectException {
        this.connection = new MySQLConnection(this.toDataSource());

        return this.connection;
    }

    private DataSource toDataSource() {
        MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setServerName(this.address.getHostName());
        dataSource.setPortNumber(this.address.getPort());

        if(this.database != null)
            dataSource.setDatabaseName(this.database);

        dataSource.setUser(this.userPass.user());
        dataSource.setPassword(new String(this.userPass.password()));

        return dataSource;
    }

    /**
     * Creates a new {@link MySQLConnector} and returns it.
     * The created {@link MySQLConnector} is also automatically added to the {@link ConnectorsService}.
     * @param address The {@link InetSocketAddress} that the connector points to.
     * @param userPass The {@link UserPass} to be used when authenticating with the remote resource.
     * @param database The database to access.
     * @return A {@link MySQLConnector}.
     */
    public static MySQLConnector create(InetSocketAddress address, UserPass userPass, String database) {
        return new MySQLConnector(address, userPass, database);
    }
}
