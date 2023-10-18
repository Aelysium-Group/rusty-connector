package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.core.lib.model.UserPass;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import one.microstream.afs.sql.types.SqlConnector;
import one.microstream.afs.sql.types.SqlFileSystem;
import one.microstream.afs.sql.types.SqlProviderMariaDb;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import one.microstream.storage.types.StorageConfiguration;
import org.mariadb.jdbc.MariaDbDataSource;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class MySQLStorage extends Service {
    protected String jdbc;
    protected InetSocketAddress address;
    protected UserPass userPass;
    protected String database;
    protected EmbeddedStorageManager storageManager;

    protected MySQLStorage(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
        this.address = address;
        this.userPass = userPass;
        this.database = database;

        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl("jdbc:mysql://"+address.getHostName()+":"+address.getPort()+"/"+database);
        dataSource.setUser(userPass.user());
        dataSource.setPassword(new String(userPass.password()));

        SqlFileSystem fileSystem = SqlFileSystem.New(
                SqlConnector.Caching(
                        SqlProviderMariaDb.New(dataSource)
                )
        );

        final EmbeddedStorageManager storageManager = EmbeddedStorage.start();

        if(storageManager.root() == null) {
            storageManager.setRoot(new StorageRoot());
            storageManager.storeRoot();
        }

        this.storageManager = storageManager;
    }

    @Override
    public void kill() {
        this.storageManager.shutdown();
    }

    public EmbeddedStorageManager storageManager() {
        return storageManager;
    }

    public static MySQLStorage create(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
        return new MySQLStorage(address, userPass, database);
    }
}
