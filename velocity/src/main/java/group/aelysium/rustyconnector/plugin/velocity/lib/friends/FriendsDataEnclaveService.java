package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.Cache;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

/**
 * The data enclave service allows you to store database responses in-memory
 * to be used later.
 * If a value is available in-memory, data enclave will return that.
 * If not, it will query the database.
 */
public class FriendsDataEnclaveService extends Service {
    private final Map<FakePlayer, Long> players = new HashMap<>();
    private final Cache<List<FriendMapping>> cache = new Cache<>(100); // Max number of players that can be stored at once
    private final FriendsMySQLService mySQLService;

    public FriendsDataEnclaveService(FriendsMySQLService mySQLService) {
        this.mySQLService = mySQLService;
    }

    /**
     * Get the mappings associated with a player.
     * If there are no cache entries for a player, create one and return it.
     * @param player The player.
     * @return A cache entry list.
     */
    private List<FriendMapping> getPlayersCacheEntry(FakePlayer player) {
        try {
            Long snowflake = this.players.get(player);
            if(snowflake == null) throw new NoOutputException();

            List<FriendMapping> mappings = this.cache.get(snowflake);
            if(mappings == null) throw new NoOutputException();

            return mappings;
        } catch (Exception ignore) {}
        List<FriendMapping> mappings = new ArrayList<>();
        Long snowflake = this.cache.put(mappings);

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

    public boolean uncachePlayer(FakePlayer player) {
        try {
            Long snowflake = this.players.get(player);
            this.players.remove(player);
            this.cache.get(snowflake).clear();
            this.cache.remove(snowflake);
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
    public Optional<List<FriendMapping>> findFriends(Player player, boolean forcePull) {
        if(!forcePull)
            try {
                return Optional.of(this.getPlayersCacheEntry(FakePlayer.from(player)));
            } catch (Exception ignore) {}

        try {
            List<FriendMapping> mappings = this.mySQLService.findFriends(player).orElseThrow();
            mappings.forEach(this::putMapping);

            return Optional.of(mappings);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    /**
     * Check if two players are friends.
     * If the players are found as friends in the cache, returns `true`.
     * If not, search MySQL and return whether they are friends.
     * @param player1 The first player.
     * @param player2 The second player.
     * @return `true` If the two players are friends in the cache, or on MySQL.
     */
    public boolean areFriends(FakePlayer player1, FakePlayer player2) throws RuntimeException {
        if (
                this.getPlayersCacheEntry(player1).stream().anyMatch(friendMapping -> friendMapping.friendOf(player1).equals(player2)) ||
                this.getPlayersCacheEntry(player2).stream().anyMatch(friendMapping -> friendMapping.friendOf(player2).equals(player1))
        )
            return true;

        return this.mySQLService.areFriends(player1, player2);
    }

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     * @throws SQLException If there was an issue.
     */
    public Optional<Integer> getFriendCount(Player player) {
        try {
            return Optional.of(this.getPlayersCacheEntry(FakePlayer.from(player)).size());
        } catch (Exception ignore) {}

        try {
            List<FriendMapping> mappings = this.mySQLService.findFriends(player).orElseThrow();
            Long snowflake = this.cache.put(mappings);
            this.players.put(FakePlayer.from(player), snowflake);

            return Optional.of(mappings.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(Player player1, Player player2) throws SQLException {
        try {
            FriendMapping mapping = new FriendMapping(player1, player2);
            try {
                 this.mySQLService.addFriend(player1, player2);
            } catch (SQLIntegrityConstraintViolationException ignore) {}

            putMapping(mapping);

            return Optional.of(mapping);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public void removeFriend(Player player1, Player player2) throws SQLException {
        FriendMapping mapping = new FriendMapping(player1, player2);
        try {
            this.mySQLService.removeFriend(player1, player2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        removeMapping(mapping);
    }

    @Override
    public void kill() {
        this.cache.getAll().forEach(List::clear);
        this.cache.empty();
        this.players.clear();
    }
}
