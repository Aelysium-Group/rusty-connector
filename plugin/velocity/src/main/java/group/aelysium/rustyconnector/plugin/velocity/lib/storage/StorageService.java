package group.aelysium.rustyconnector.plugin.velocity.lib.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors.MySQLReactor;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors.StorageReactor;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IStorageService;
import group.aelysium.rustyconnector.toolkit.core.UserPass;

import java.net.InetSocketAddress;
import java.sql.SQLException;

public class StorageService implements IStorageService {
    protected final Database database;
    protected final StorageConfiguration config;
    protected StorageService(StorageConfiguration config) throws SQLException {
        this.config = config;
        this.database = new Database(this.config.reactor());
    }

    public Database database() {
        return this.database;
    }

    @Override
    public void kill() {
        this.database.kill();
    }

    public static StorageService create(StorageConfiguration configuration) throws SQLException {
        return new StorageService(configuration);
    }

    public enum StorageType {
        SQLITE,
        MYSQL
    }

    public static abstract class StorageConfiguration {
        protected final StorageType type;

        protected StorageConfiguration(StorageType type) {
            this.type = type;
        }

        public StorageType type() {
            return this.type;
        }
        public abstract StorageReactor reactor();

        public static class MySQL extends StorageConfiguration {
            private final MySQLReactor.Core.Settings settings;

            public MySQL(InetSocketAddress address, UserPass userPass, String database) {
                super(StorageType.MYSQL);
                this.settings = new MySQLReactor.Core.Settings(address, userPass, database);
            }

            public StorageReactor reactor() {
                return new MySQLReactor(settings);
            }

            public InetSocketAddress address() {
                return this.settings.address();
            }
            public UserPass userPass() {
                return this.settings.userPass();
            }
            public String database() {
                return this.settings.database();
            }
        }
    }
}
