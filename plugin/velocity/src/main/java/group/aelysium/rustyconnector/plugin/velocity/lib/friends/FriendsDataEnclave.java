package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.api.velocity.friends.IFriendsDataEnclave;
import group.aelysium.rustyconnector.api.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageRoot;

import java.util.*;

public class FriendsDataEnclave implements IFriendsDataEnclave<ResolvablePlayer, FriendMapping> {
    private final MySQLStorage storage;

    public FriendsDataEnclave(IMySQLStorageService storage) {
        this.storage = (MySQLStorage) storage;
    }

    public Optional<List<FriendMapping>> findFriends(ResolvablePlayer player) {
        try {
            StorageRoot root = this.storage.root();

            List<FriendMapping> mappings = root.friends().stream()
                    .filter(friendMapping -> friendMapping.player1().equals(player) || friendMapping.player2().equals(player))
                    .toList();

            return Optional.of(mappings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public boolean areFriends(ResolvablePlayer player1, ResolvablePlayer player2) throws RuntimeException {
        StorageRoot root = this.storage.root();
        return root.friends().contains(new FriendMapping(player1, player2));
    }

    public Optional<Long> getFriendCount(ResolvablePlayer player) {
        try {
            StorageRoot root = this.storage.root();
            long count = root.friends().stream().filter(friendMapping -> friendMapping.contains(player)).count();

            return Optional.of(count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(ResolvablePlayer player1, ResolvablePlayer player2) {
        try {
            StorageRoot root = this.storage.root();

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
            StorageRoot root = this.storage.root();

            FriendMapping friendMapping = FriendMapping.from(player1, player2);

            List<FriendMapping> networkFriends = root.friends();
            networkFriends.remove(friendMapping);

            this.storage.store(networkFriends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
