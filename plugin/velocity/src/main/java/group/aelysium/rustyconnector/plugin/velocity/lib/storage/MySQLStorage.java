package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.core.lib.model.UserPass;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;
import one.microstream.afs.sql.types.SqlConnector;
import one.microstream.afs.sql.types.SqlFileSystem;
import one.microstream.afs.sql.types.SqlProviderMariaDb;
import one.microstream.concurrency.XThreads;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.mariadb.jdbc.MariaDbDataSource;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class MySQLStorage extends Service {
    protected InetSocketAddress address;
    protected UserPass userPass;
    protected String database;
    protected EmbeddedStorageManager storageManager;

    protected MySQLStorage(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
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
                new StorageRoot(),
                fileSystem.ensureDirectoryPath("storage")
        );

        storageManager.storeRoot();

        this.storageManager = storageManager;
    }

    public StorageRoot root() {
        return (StorageRoot) this.storageManager.root();
    }

    /**
     * Convenience method to store the passed object synchronously.
     * Running this is equivalent to running
     * <pre>
     * XThreads.executeSynchronized(() -> {
     *      storageManager.store(object);
     * });
     * </pre>
     * Objects should be stored in accordance with <a href="https://docs.microstream.one/manual/storage/storing-data/index.html">the MicroStream docs</a>.
     * If you create a new object, you store the parent of that object.
     * If you modify and object, you store the object itself.
     * @param object The object to store. You should store objects based on <a href="https://docs.microstream.one/manual/storage/storing-data/index.html">"The Object that has been modified has to be stored."</a>
     */
    public void store(Object object) {
        XThreads.executeSynchronized(() -> {
            storageManager.store(object);
        });
    }

    @Override
    public void kill() {
        this.storageManager.shutdown();
    }

    public static MySQLStorage create(InetSocketAddress address, UserPass userPass, String database) throws SQLException {
        return new MySQLStorage(address, userPass, database);
    }
}
