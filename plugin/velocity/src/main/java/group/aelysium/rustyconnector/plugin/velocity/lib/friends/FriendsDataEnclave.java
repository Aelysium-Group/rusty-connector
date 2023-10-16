package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.connectors.storage.*;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Destroyable;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Destructive;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.SyncedResource;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Unsynced;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.hash.Snowflake;
import group.aelysium.rustyconnector.core.lib.model.DataEnclave;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerDataEnclave;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class FriendsDataEnclave extends DataEnclave<Long, List<FriendsDataEnclave.FriendMapping>> {
    private final Map<PlayerDataEnclave.FakePlayer, Long> players = new HashMap<>();
    protected Snowflake snowflake = new Snowflake();

    public FriendsDataEnclave(MySQLConnector connector) throws Exception {
        super(connector, 50, LiquidTimestamp.from(30, TimeUnit.MINUTES));
        StorageConnection connection = connector.connection().orElseThrow();

        String mysql = "";
        try(InputStream stream = Tinder.get().resourceAsStream("mysql/players.sql")) {
            mysql = new String(stream.readAllBytes());
        } catch (Exception ignore) {}

        StorageQuery initialize = StorageQuery.create(mysql);
        connection.query(initialize);
    }

    /**
     * Get the mappings associated with a player.
     * If there are no cache entries for a player, create one and return it.
     * @param player The player.
     * @return A cache entry list.
     */
    private List<FriendsDataEnclave.FriendMapping> getPlayersCacheEntry(PlayerDataEnclave.FakePlayer player) {
        try {
            Long snowflake = this.players.get(player);
            if(snowflake == null) throw new NoOutputException();

            List<FriendsDataEnclave.FriendMapping> mappings = this.cache.getIfPresent(snowflake);
            if(mappings == null) throw new NoOutputException();

            return mappings;
        } catch (Exception ignore) {}
        List<FriendsDataEnclave.FriendMapping> mappings = new ArrayList<>();
        Long snowflake = this.snowflake.nextId();

        this.cache.put(snowflake, mappings);
        this.players.put(player, snowflake);

        return mappings;
    }

    private void putMapping(FriendMapping mapping) {
        getPlayersCacheEntry(mapping.player1()).remove(mapping);
        getPlayersCacheEntry(mapping.player2()).remove(mapping);
    }

    private void removeMapping(FriendMapping mapping) {
        List<FriendMapping> player1Mappings = getPlayersCacheEntry(mapping.player1());
        List<FriendMapping> player2Mappings = getPlayersCacheEntry(mapping.player2());

        player1Mappings.remove(mapping);
        player2Mappings.remove(mapping);

        if(player1Mappings.size() == 0) uncachePlayer(mapping.player1());
        if(player2Mappings.size() == 0) uncachePlayer(mapping.player2());
    }

    public boolean uncachePlayer(PlayerDataEnclave.FakePlayer player) {
        try {
            Long snowflake = this.players.get(player);
            this.players.remove(player);
            this.cache.getIfPresent(snowflake).clear();
            this.cache.invalidate(snowflake);
        } catch (Exception ignore) {}
        return false;
    }

    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @param forcePull Should we pull directly from MySQL?
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public Optional<List<FriendsDataEnclave.FriendMapping>> findFriends(PlayerDataEnclave.FakePlayer player, boolean forcePull) {
        if(!forcePull)
            try {
                return Optional.of(this.getPlayersCacheEntry(player));
            } catch (Exception ignore) {}

        try {
            List<FriendsDataEnclave.FriendMapping> mappings = FriendsDataEnclave.FriendMapping.findFriends(this.connector, player).orElseThrow();
            mappings.forEach(this::putMapping);

            return Optional.of(mappings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Check if two players are friends.
     * @param player1 The first player.
     * @param player2 The second player.
     * @return `true` If the two players are friends.
     */
    public boolean areFriends(PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) throws RuntimeException {
        return FriendsDataEnclave.FriendMapping.areFriends(this.connector, player1, player2);
    }

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     * @throws SQLException If there was an issue.
     */
    public Optional<Integer> getFriendCount(PlayerDataEnclave.FakePlayer player) {
        try {
            return Optional.of(this.getPlayersCacheEntry(player).size());
        } catch (Exception ignore) {}

        try {
            List<FriendMapping> mappings = FriendsDataEnclave.FriendMapping.findFriends(this.connector, player).orElseThrow();

            Long snowflake = this.snowflake.nextId();

            this.cache.put(snowflake, mappings);
            this.players.put(player, snowflake);

            return Optional.of(mappings.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) {
        try {
            FriendMapping friendMapping = FriendMapping.from(player1, player2);
            try {
                friendMapping.sync(this.connector);
            } catch (Exception e) {
                e.printStackTrace();
            }

            putMapping(friendMapping);

            return Optional.of(friendMapping);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void removeFriend(PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) throws SQLException {
        FriendMapping friendMapping = FriendMapping.from(player1, player2);

        removeMapping(friendMapping);

        try {
            friendMapping.sync(this.connector);
            friendMapping.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {
        this.players.clear();
        this.cache.asMap().values().forEach(List::clear);
        this.cache.invalidateAll();
    }

    protected static class FriendMapping extends SyncedResource {
        private static final StorageQuery FIND_FRIENDS = StorageQuery.create(
                "SELECT * FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;"
        );
        private static final StorageQuery DELETE_FRIEND = StorageQuery.create(
                "DELETE FROM friends WHERE player1_uuid = ? AND player2_uuid = ?;"
        );
        private static final StorageQuery REPLACE_INSERT_FRIEND = StorageQuery.create(
                "REPLACE INTO friends (player1_uuid, player2_uuid) VALUES(?, ?);"
        );

        protected PlayerDataEnclave.FakePlayer player1;
        protected PlayerDataEnclave.FakePlayer player2;

        private FriendMapping(StorageConnector<?> connector, PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) {
            super(connector);
            // Ensure that players are always in order of the lowest uuid to the highest uuid.
            if(player1.uuid().compareTo(player2.uuid()) > 0) {
                this.player1 = player2;
                this.player2 = player1;

                return;
            }

            this.player1 = player1;
            this.player2 = player2;
        }

        protected FriendMapping(PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) {
            // Ensure that players are always in order of the lowest uuid to the highest uuid.
            if(player1.uuid().compareTo(player2.uuid()) > 0) {
                this.player1 = player2;
                this.player2 = player1;

                return;
            }

            this.player1 = player1;
            this.player2 = player2;
        }

        @Destroyable
        public PlayerDataEnclave.FakePlayer player1() {
            this.throwDestroyed();
            return this.player1;
        };
        @Destroyable
        public PlayerDataEnclave.FakePlayer player2() {
            this.throwDestroyed();
            return this.player2;
        };

        /**
         * Return the friend of one of the players in this mapping.
         * @param player The player to get the friend of.
         * @return The friend of `player`.
         */
        @Destroyable
        public PlayerDataEnclave.FakePlayer friendOf(PlayerDataEnclave.FakePlayer player) {
            this.throwDestroyed();

            if(this.player1.equals(player)) return this.player2;
            if(this.player2.equals(player)) return this.player1;

            throw new NullPointerException("This mapping doesn't apply to the provided player!");
        }

        /**
         * Return the friend of one of the players in this mapping.
         * @param player The player to get the friend of.
         * @return The friend of `player`.
         */
        @Destroyable
        public PlayerDataEnclave.FakePlayer friendOf(Player player) {
            this.throwDestroyed();

            PlayerDataEnclave.FakePlayer fakePlayer = PlayerDataEnclave.FakePlayer.from(player);

            if(this.player1.equals(fakePlayer)) return this.player2;
            if(this.player2.equals(fakePlayer)) return this.player1;

            throw new NullPointerException("This mapping doesn't apply to the provided player!");
        }

        @Override
        @Destroyable
        public boolean equals(Object o) {
            this.throwDestroyed();

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FriendMapping that = (FriendMapping) o;
            return Objects.equals(player1, that.player1) && Objects.equals(player2, that.player2);
        }

        @Unsynced
        public static FriendMapping from(PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) {
            return new FriendMapping(player1, player2);
        }

        @Override
        @Destroyable
        public void sync(StorageConnector<?> connector) throws Exception {
            this.throwDestroyed();

            StorageConnection connection = this.connector.connection().orElseThrow();

            connection.query(REPLACE_INSERT_FRIEND, this.player1().uuid().toString(), this.player2().uuid().toString());

            this.connector = connector;
            this.synced = true;
        }

        @Override
        @Destructive
        public void delete() throws Exception {
            this.throwDestroyed();

            StorageConnection connection = this.connector.connection().orElseThrow();

            connection.query(DELETE_FRIEND, this.player1().uuid().toString(), this.player2().uuid().toString());

            this.destroyed = true;
        }

        protected static Optional<List<FriendMapping>> findFriends(StorageConnector<?> connector, PlayerDataEnclave.FakePlayer player) {
            StorageConnection connection = connector.connection().orElseThrow();
            Tinder api = Tinder.get();
            PlayerService playerService = api.services().playerService().orElseThrow();

            try {
                List<FriendMapping> friends = new ArrayList<>();

                Consumer<StorageResponse<?>> consumer = (result) -> {
                    try {
                        result.forEach(object -> {
                            ResultSet row = (ResultSet) object;

                            try {
                                PlayerDataEnclave.FakePlayer player1 = playerService.dataEnclave().get(UUID.fromString(row.getString("player1_uuid"))).orElseThrow();
                                PlayerDataEnclave.FakePlayer player2 = playerService.dataEnclave().get(UUID.fromString(row.getString("player2_uuid"))).orElseThrow();

                                if (player1 == null) return;
                                if (player2 == null) return;

                                friends.add(new FriendMapping(connector, player1, player2));
                            } catch (Exception ignore) {}
                        });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                connection.query(FIND_FRIENDS, consumer, player.uuid().toString(), player.uuid().toString());

                return Optional.of(friends);
            } catch (Exception e) {
                api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            }

            return Optional.empty();
        }

        protected static boolean areFriends(StorageConnector<?> connector, PlayerDataEnclave.FakePlayer player1, PlayerDataEnclave.FakePlayer player2) {
            StorageConnection<StorageResponse<?>> connection = connector.connection().orElseThrow();
            Tinder api = Tinder.get();
            FriendMapping orderedMapping = new FriendMapping(player1, player2);

            try {
                AtomicBoolean areFriends = new AtomicBoolean(false);

                Consumer<StorageResponse<?>> consumer = (result) -> {
                    areFriends.set(result.rows() > 0);
                };

                connection.query(FIND_FRIENDS, consumer, orderedMapping.player1().uuid().toString(), orderedMapping.player2().uuid().toString());
                return areFriends.get();
            } catch (Exception e) {
                api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            }

            return false;
        }
    }

}
