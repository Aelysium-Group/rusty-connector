package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendsDataEnclave;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;

import java.util.*;

public class FriendsDataEnclave implements IFriendsDataEnclave {
    private final StorageService storage;

    public FriendsDataEnclave(IMySQLStorageService storage) {
        this.storage = (StorageService) storage;
    }

    public Optional<List<FriendMapping>> findFriends(IPlayer player) {
        try {
            Database root = this.storage.database();

            List<FriendMapping> mappings = root.friends().stream()
                    .filter(friendMapping -> friendMapping.player1().equals(player) || friendMapping.player2().equals(player))
                    .toList();

            return Optional.of(mappings);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public boolean areFriends(IPlayer player1, IPlayer player2) throws RuntimeException {
        Database root = this.storage.database();
        return root.friends().contains(FriendMapping.from(player1, player2));
    }

    public Optional<Long> getFriendCount(IPlayer player) {
        try {
            Database root = this.storage.database();
            long count = root.friends().stream().filter(friendMapping -> friendMapping.contains((group.aelysium.rustyconnector.plugin.velocity.lib.players.Player) player)).count();

            return Optional.of(count);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(IPlayer player1, IPlayer player2) {
        try {
            Database root = this.storage.database();

            FriendMapping friendMapping = FriendMapping.from(player1, player2);

            Set<FriendMapping> networkFriends = root.friends();
            networkFriends.add(friendMapping);

            this.storage.store(networkFriends);

            return Optional.of(friendMapping);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void removeFriend(IPlayer player1, IPlayer player2) {
        try {
            Database root = this.storage.database();

            FriendMapping friendMapping = FriendMapping.from(player1, player2);

            Set<FriendMapping> networkFriends = root.friends();
            networkFriends.remove(friendMapping);

            this.storage.store(networkFriends);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
