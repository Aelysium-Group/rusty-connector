package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.toolkit.core.UserPass;
import org.eclipse.store.afs.sql.types.SqlConnector;
import org.eclipse.store.afs.sql.types.SqlFileSystem;
import org.eclipse.store.afs.sql.types.SqlProviderMariaDb;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.mariadb.jdbc.MariaDbDataSource;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class StorageService implements IMySQLStorageService {
    protected InetSocketAddress address;
    protected UserPass userPass;
    protected String database;
    protected EmbeddedStorageManager storageManager;

    protected StorageService(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
        this.address = address;
        this.userPass = userPass;
        this.database = database;

        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl("jdbc:mysql://"+address.getHostName()+":"+address.getPort()+"/"+database+"?usePipelineAuth=false&useBatchMultiSend=false");
        dataSource.setUser(userPass.user());
        dataSource.setPassword(new String(userPass.password()));

        SqlFileSystem fileSystem = SqlFileSystem.New(
                SqlConnector.Caching(
                        SqlProviderMariaDb.New(dataSource)
                )
        );

        final EmbeddedStorageManager storageManager = EmbeddedStorage.start(
                new Database(),
                fileSystem.ensureDirectoryPath("storage")
        );

        storageManager.storeRoot();

        this.storageManager = storageManager;
    }

    public Database root() {
        return (Database) this.storageManager.root();
    }

    public void store(Object object) {
        storageManager.store(object);
    }

    @Override
    public void kill() {
        this.storageManager.shutdown();
    }

    public static StorageService create(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
        return new StorageService(address, userPass, database);
    }
}
