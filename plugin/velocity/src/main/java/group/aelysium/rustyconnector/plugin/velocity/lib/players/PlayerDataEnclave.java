package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.connectors.storage.*;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.SyncedResource;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Unsynced;
import group.aelysium.rustyconnector.core.lib.model.DataEnclave;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;

import java.io.InputStream;
import java.io.SyncFailedException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlayerDataEnclave extends DataEnclave<PlayerDataEnclave.FakePlayer, Boolean> {
    public PlayerDataEnclave(StorageConnector<?> connector) throws Exception {
        super(connector, 200, LiquidTimestamp.from(30, TimeUnit.MINUTES));
        StorageConnection connection = connector.connection().orElseThrow();

        String mysql = "";
        try(InputStream stream = Tinder.get().resourceAsStream("mysql/players.sql")) {
            mysql = new String(stream.readAllBytes());
        } catch (Exception ignore) {}

        StorageQuery initialize = StorageQuery.create(mysql);
        connection.query(initialize);
    }

    protected Stream<FakePlayer> filter(Predicate<FakePlayer> predicate) {
        return this.cache.asMap().keySet().stream().filter(predicate);
    }

    public void cachePlayer(Player player) {
        this.cache.put(FakePlayer.from(player), false);
    }
    public void cachePlayer(FakePlayer player) {
        this.cache.put(player, false);
    }

    public Optional<FakePlayer> get(UUID uuid) throws SyncFailedException {
        // Check velocity for online players first
        try {
            FakePlayer fakePlayer = FakePlayer.from(Tinder.get().velocityServer().getPlayer(uuid).orElseThrow());
            cachePlayer(fakePlayer);

            return Optional.of(fakePlayer);
        } catch (Exception ignore) {}

        // Check the local cache for a matching player
        try {
            return this.filter(entry -> entry.uuid().equals(uuid)).findFirst();
        } catch (Exception ignore) {}

        // Ask MySQL for the player and then cache it.
        try {
            FakePlayer fakePlayer = FakePlayer.fetch(this.connector, uuid);

            this.cachePlayer(fakePlayer);

            return Optional.of(fakePlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FakePlayer> get(String username) throws SyncFailedException {
        // Check velocity for online players first
        try {
            FakePlayer fakePlayer = FakePlayer.from(Tinder.get().velocityServer().getPlayer(username).orElseThrow());
            cachePlayer(fakePlayer);

            return Optional.of(fakePlayer);
        } catch (Exception ignore) {}

        // Check the local cache for a matching player
        try {
            return this.filter(entry -> entry.username().equals(username)).findFirst();
        } catch (Exception ignore) {}

        // Ask MySQL for the player and then cache it.
        try {
            FakePlayer fakePlayer = FakePlayer.fetch(this.connector, username);

            this.cachePlayer(fakePlayer);

            return Optional.of(fakePlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void savePlayer(Player player) {
        try {
            FakePlayer fakePlayer = FakePlayer.from(player);
            fakePlayer.sync(this.connector);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {
        this.cache.invalidateAll();
    }

    public static class FakePlayer extends SyncedResource {
        private static final StorageQuery FIND_PLAYER_UUID = StorageQuery.create(
                "SELECT * FROM players WHERE uuid = ?;"
        );
        private static final StorageQuery FIND_PLAYER_USERNAME = StorageQuery.create(
                "SELECT * FROM players WHERE username = ?;"
        );
        private static final StorageQuery REPLACE_INSERT_PLAYER = StorageQuery.create(
                "REPLACE INTO players (uuid, username) VALUES(?, ?);"
        );

        protected UUID uuid;
        protected String username;

        protected FakePlayer(StorageConnector<?> connector, UUID uuid, String username) {
            super(connector);
            this.uuid = uuid;
            this.username = username;
        }
        protected FakePlayer(UUID uuid, String username) {
            super();
            this.uuid = uuid;
            this.username = username;
        }

        public UUID uuid() { return this.uuid; }
        public String username() { return this.username; }

        public Optional<Player> resolve() {
            return Tinder.get().velocityServer().getPlayer(this.uuid);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FakePlayer that = (FakePlayer) o;
            return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uuid, username);
        }

        @Override
        public void sync(StorageConnector<?> connector) throws Exception {
            StorageConnection connection = connector.connection().orElseThrow();

            connection.query(REPLACE_INSERT_PLAYER, this.uuid.toString(), this.username);

            this.connector = connector;
            this.synced = true;
        }

        @Unsynced
        public static FakePlayer from(Player player) {
            return new FakePlayer(player.getUniqueId(), player.getUsername());
        }

        /**
         * Searches the remote resource for the FakePlayer and fetches it if it's found.
         */
        protected static FakePlayer fetch(StorageConnector<?> connector, UUID uuid) throws Exception {
            StorageConnection connection = connector.connection().orElseThrow();

            try {
                AtomicReference<FakePlayer> player = null;

                Consumer<StorageResponse<?>> consumer = (result) -> {

                    ResultConsumer resultConsumer = new ResultConsumer(result);
                    AtomicReference<String> username = new AtomicReference<>();

                    resultConsumer.forMySQL((rows) -> {
                        try {
                            username.set(rows.first().getString("username"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    resultConsumer.trigger();

                    player.set(new FakePlayer(connector, uuid, username.get()));
                };

                connection.query(FIND_PLAYER_UUID, consumer, uuid.toString());

                return player.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        protected static FakePlayer fetch(StorageConnector<?> connector, String username) throws Exception {
            StorageConnection connection = connector.connection().orElseThrow();
            AtomicReference<String> uuid = new AtomicReference<>();

            try {
                Consumer<StorageResponse<?>> consumer = (result) -> {
                    ResultConsumer resultConsumer = result.consumer();
                    resultConsumer.forMySQL((rows) -> {
                        try {
                            uuid.set(rows.first().getString("uuid"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    resultConsumer.trigger();
                };

                connection.query(FIND_PLAYER_USERNAME, consumer, username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return new FakePlayer(connector, UUID.fromString(uuid.get()), username);
        }

        @Override
        public void delete() {}
    }
}
