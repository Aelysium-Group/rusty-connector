package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageRoot;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.sql.SQLException;
import java.util.*;

public class FriendsDataEnclave {
    private final EmbeddedStorageManager storage;

    public FriendsDataEnclave(MySQLStorage storage) {
        this.storage = storage.storageManager();
    }

    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public Optional<List<FriendMapping>> findFriends(ResolvablePlayer player) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            List<FriendMapping> mappings = root.friends().stream()
                    .filter(friendMapping -> friendMapping.player1().equals(player) || friendMapping.player2().equals(player))
                    .toList();

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
    public boolean areFriends(ResolvablePlayer player1, ResolvablePlayer player2) throws RuntimeException {
        StorageRoot root = (StorageRoot) this.storage.root();
        return root.friends().contains(new FriendMapping(player1, player2));
    }

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     * @throws SQLException If there was an issue.
     */
    public Optional<Long> getFriendCount(ResolvablePlayer player) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();
            long count = root.friends().stream().filter(friendMapping -> friendMapping.contains(player)).count();

            return Optional.of(count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(ResolvablePlayer player1, ResolvablePlayer player2) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            FriendMapping friendMapping = FriendMapping.from(player1, player2);

            List<FriendMapping> networkFriends = root.friends();
            networkFriends.add(friendMapping);

            this.storage.store(networkFriends);

            return Optional.of(friendMapping);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void removeFriend(ResolvablePlayer player1, ResolvablePlayer player2) {
        try {
            StorageRoot root = (StorageRoot) this.storage.root();

            FriendMapping friendMapping = FriendMapping.from(player1, player2);

            List<FriendMapping> networkFriends = root.friends();
            networkFriends.remove(friendMapping);

            this.storage.store(networkFriends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
