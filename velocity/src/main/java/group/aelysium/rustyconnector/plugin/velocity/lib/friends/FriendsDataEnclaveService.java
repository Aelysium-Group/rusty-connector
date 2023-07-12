package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.Cache;
import group.aelysium.rustyconnector.core.lib.model.Service;

import java.sql.SQLException;
import java.util.*;

/**
 * The data enclave service allows you to store database responses in-memory
 * to be used later.
 * If a value is available in-memory, data enclave will return that.
 * If not, it will query the database.
 */
public class FriendsDataEnclaveService extends Service {
    private final Map<Player, Long> players = new HashMap<>();
    private final Cache<List<FriendMapping>> cache = new Cache<>(100); // Max number of players that can be stored at once
    private final FriendsMySQLService mySQLService;

    public FriendsDataEnclaveService(FriendsMySQLService mySQLService) {
        this.mySQLService = mySQLService;
    }

    private Optional<List<FriendMapping>> getPlayersCacheEntry(Player player) {
        try {
            return Optional.of(this.cache.get(this.players.get(player)));
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public Optional<List<FriendMapping>> findFriends(Player player) {
        try {
            return Optional.of(this.getPlayersCacheEntry(player).orElseThrow());
        } catch (Exception ignore) {}

        try {
            List<FriendMapping> mappings = this.mySQLService.findFriends(player).orElseThrow();
            Long snowflake = this.cache.put(mappings);
            this.players.put(player, snowflake);

            return Optional.of(mappings);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    /**
     * Get number of friends of a player.
     * @param player The player to get the friend count of.
     * @return The number of friends a player has.
     * @throws SQLException If there was an issue.
     */
    public Optional<Integer> getFriendCount(Player player) {
        try {
            return Optional.of(this.getPlayersCacheEntry(player).orElseThrow().size());
        } catch (Exception ignore) {}

        try {
            List<FriendMapping> mappings = this.mySQLService.findFriends(player).orElseThrow();
            Long snowflake = this.cache.put(mappings);
            this.players.put(player, snowflake);

            return Optional.of(mappings.size());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public Optional<FriendMapping> addFriend(Player player1, Player player2) throws SQLException {
        try {
            FriendMapping mapping = this.mySQLService.addFriend(player1, player2);

            try {
                this.getPlayersCacheEntry(player1).orElseThrow().add(mapping);
            } catch (Exception ignore) {}
            try {
                this.getPlayersCacheEntry(player2).orElseThrow().add(mapping);
            } catch (Exception ignore) {}

            return Optional.of(mapping);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    public void removeFriend(Player player1, Player player2) throws SQLException {
        FriendMapping mapping = new FriendMapping(player1, player2);
        try {
            this.mySQLService.removeFriend(player1, player2);
        } catch (Exception ignore) {}
        try {
            this.getPlayersCacheEntry(player1).orElseThrow().remove(mapping);
        } catch (Exception ignore) {}
        try {
            this.getPlayersCacheEntry(player2).orElseThrow().remove(mapping);
        } catch (Exception ignore) {}
    }

    @Override
    public void kill() {
        this.cache.getAll().forEach(List::clear);
        this.cache.empty();
        this.players.clear();
    }
}
