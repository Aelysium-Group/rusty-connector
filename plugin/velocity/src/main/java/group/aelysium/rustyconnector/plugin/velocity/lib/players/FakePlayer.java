package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class FakePlayer {
    protected UUID uuid;
    protected String username;

    protected FakePlayer(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID uuid() { return this.uuid; }
    public String username() { return this.username; }

    public Optional<Player> resolve() {
        return Tinder.get().velocityServer().getPlayer(this.uuid);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        FakePlayer that = (FakePlayer) object;
        return Objects.equals(uuid, that.uuid) && Objects.equals(username, that.username);
    }

    public static FakePlayer from(Player player) {
        return new FakePlayer(player.getUniqueId(), player.getUsername());
    }
    public static FakePlayer from(UUID uuid, String username) {
        return new FakePlayer(uuid, username);
    }
}

/*

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
         Searches the remote resource for the FakePlayer and fetches it if it's found.

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

    protected static FakePlayer fetch(String username) throws Exception {
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
 */