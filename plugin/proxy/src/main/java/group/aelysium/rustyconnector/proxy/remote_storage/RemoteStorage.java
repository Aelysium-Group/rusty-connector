package group.aelysium.rustyconnector.proxy.remote_storage;

import group.aelysium.rustyconnector.proxy.remote_storage.reactors.MySQLReactor;
import group.aelysium.rustyconnector.proxy.remote_storage.reactors.StorageReactor;
import group.aelysium.rustyconnector.toolkit.common.UserPass;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IRemoteStorage;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public class RemoteStorage implements IRemoteStorage, Particle {
    private final StorageReactor reactor;
    private final Players players;

    private RemoteStorage(StorageReactor reactor) {
        this.reactor = reactor;
        this.players = new Players(reactor);
    }

    public Players players() {
        return this.players;
    }

    public void close() throws Exception {
        this.reactor.close();
    }

    public static class Players extends StorageReactor.Holder implements IRemoteStorage.Players {
        public Players(StorageReactor reactor) {
            super(reactor);
        }

        public void set(IPlayer player) {
            this.reactor.savePlayer(player.uuid(), player.username());
        }

        public Optional<IPlayer> get(UUID uuid) {
            return this.reactor.fetchPlayer(uuid);
        }
        public Optional<IPlayer> get(String username) {
            return this.reactor.fetchPlayer(username);
        }
    }

    public static class Tinder extends Particle.Tinder<IRemoteStorage> {
        private final Configuration configuration;

        public Tinder(@NotNull Configuration configuration) {
            this.configuration = configuration;
        }

        @Override
        public @NotNull IRemoteStorage ignite() throws Exception {
            return new RemoteStorage(this.configuration.reactor());
        }
    }

    public enum StorageType {
        SQLITE,
        MYSQL
    }

    public static abstract class Configuration {
        protected final StorageType type;

        protected Configuration(StorageType type) {
            this.type = type;
        }

        public StorageType type() {
            return this.type;
        }
        public abstract StorageReactor reactor();

        public static class MySQL extends Configuration {
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