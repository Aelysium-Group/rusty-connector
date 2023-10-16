package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerDataEnclave;

import java.util.Objects;

public class FriendMapping {
    private final FakePlayer player1;
    private final FakePlayer player2;

    protected FriendMapping(FakePlayer player1, FakePlayer player2) {
        // Ensure that players are always in order of the lowest uuid to the highest uuid.
        if(player1.uuid().compareTo(player2.uuid()) > 0) {
            this.player1 = player2;
            this.player2 = player1;

            return;
        }

        this.player1 = player1;
        this.player2 = player2;
    }

    public FakePlayer player1() {
        return player1;
    }

    public FakePlayer player2() {
        return player2;
    }

    public boolean contains(FakePlayer player) {
        return this.player1.equals(player) || this.player2.equals(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendMapping that = (FriendMapping) o;
        return Objects.equals(player1, that.player1) && Objects.equals(player2, that.player2);
    }

    public static FriendMapping from(FakePlayer player1, FakePlayer player2) {
        return new FriendMapping(player1, player2);
    }

    public FakePlayer fetchOther(FakePlayer player) {
        if(this.player1.equals(player)) return this.player2;
        if(this.player2.equals(player)) return this.player1;

        return null;
    }
}
/**

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

@Destroyable
public PlayerDataEnclave.FakePlayer friendOf(PlayerDataEnclave.FakePlayer player) {
    this.throwDestroyed();

    if(this.player1.equals(player)) return this.player2;
    if(this.player2.equals(player)) return this.player1;

    throw new NullPointerException("This mapping doesn't apply to the provided player!");
}

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
 */