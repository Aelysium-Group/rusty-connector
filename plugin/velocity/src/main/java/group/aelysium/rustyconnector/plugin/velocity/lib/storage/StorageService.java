package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.toolkit.core.UserPass;
import org.eclipse.serializer.afs.types.AFileSystem;
import org.eclipse.store.afs.nio.types.NioFileSystem;
import org.eclipse.store.afs.sql.types.SqlConnector;
import org.eclipse.store.afs.sql.types.SqlFileSystem;
import org.eclipse.store.afs.sql.types.SqlProviderMariaDb;
import org.eclipse.store.storage.embedded.types.EmbeddedStorage;
import org.eclipse.store.storage.embedded.types.EmbeddedStorageManager;
import org.eclipse.store.storage.restservice.types.StorageRestService;
import org.eclipse.store.storage.restservice.types.StorageRestServiceResolver;
import org.mariadb.jdbc.MariaDbDataSource;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class StorageService implements IMySQLStorageService {
    protected final StorageConfiguration configuration;
    protected final EmbeddedStorageManager storageManager;
    protected StorageRestService restService;

    protected StorageService(StorageConfiguration configuration) throws SQLException {
        this.configuration = configuration;

        final EmbeddedStorageManager storageManager = EmbeddedStorage.start(
                new Database(),
                this.configuration.fileSystem().ensureDirectoryPath("storage")
        );

        storageManager.storeRoot();

        if(this.configuration.enableRESTAPI()) restService = StorageRestServiceResolver.resolve(storageManager);

        this.storageManager = storageManager;
    }

    public Database database() {
        return (Database) this.storageManager.root();
    }

    public void store(Object object) {
        storageManager.store(object);
    }

    @Override
    public void kill() {
        this.storageManager.shutdown();
    }

    public static StorageService create(StorageConfiguration configuration) throws SQLException {
        return new StorageService(configuration);
    }

    public enum StorageType {
        FILE,
        MARIADB
    }

    public static abstract class StorageConfiguration {
        protected final StorageType type;
        protected final boolean enableRESTAPI;

        protected StorageConfiguration(StorageType type, boolean enableRESTAPI) {
            this.type = type;
            this.enableRESTAPI = enableRESTAPI;
        }

        public StorageType type() {
            return this.type;
        }
        public boolean enableRESTAPI() {
            return this.enableRESTAPI;
        }

        public abstract AFileSystem fileSystem();

        public static class MariaDB extends StorageConfiguration {
            protected final InetSocketAddress address;
            protected final UserPass userPass;
            protected final String database;

            public MariaDB(boolean enableRESTAPI, InetSocketAddress address, UserPass userPass, String database) {
                super(StorageType.MARIADB, enableRESTAPI);
                this.address = address;
                this.userPass = userPass;
                this.database = database;
            }

            public SqlFileSystem fileSystem() {
                try {
                    MariaDbDataSource dataSource = new MariaDbDataSource();
                    dataSource.setUrl("jdbc:mysql://" + address.getHostName() + ":" + address.getPort() + "/" + database + "?usePipelineAuth=false&useBatchMultiSend=false");
                    dataSource.setUser(userPass.user());
                    dataSource.setPassword(new String(userPass.password()));

                    SqlFileSystem fileSystem = SqlFileSystem.New(
                            SqlConnector.Caching(
                                    SqlProviderMariaDb.New(dataSource)
                            )
                    );

                    return fileSystem;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            public InetSocketAddress address() {
                return address;
            }
            public UserPass userPass() {
                return userPass;
            }
            public String database() {
                return database;
            }
        }

        public static class File extends StorageConfiguration {
            public File(boolean enableRESTAPI) {
                super(StorageType.FILE, enableRESTAPI);
            }

            @Override
            public AFileSystem fileSystem() {
                return NioFileSystem.New();
            }
        }
    }
}
