package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.Cache;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;

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
    private final Map<Player, Long> players = new HashMap<>();
    private final Cache<List<FriendMapping>> cache = new Cache<>(100); // Max number of players that can be stored at once
    private final FriendsMySQLService mySQLService;

    public FriendsDataEnclaveService(FriendsMySQLService mySQLService) {
        this.mySQLService = mySQLService;
    }

    private Optional<List<FriendMapping>> getPlayersCacheEntry(Player player) {
        try {
            Long snowflake = this.players.get(player);
            if(snowflake == null) return Optional.empty();

            List<FriendMapping> mappings = this.cache.get(snowflake);

            return Optional.of(mappings);
        } catch (Exception ignore) {
            Long snowflake = this.cache.put(new ArrayList<>());
            List<FriendMapping> mappings = this.cache.get(snowflake);

            this.players.put(player, snowflake);

            return Optional.of(mappings);
        }
    }

    public boolean unCachePlayer(Player player) {
        try {
            Long snowflake = this.players.get(player);
            this.players.remove(snowflake);
            this.cache.get(snowflake).clear();
            this.cache.remove(snowflake);
        } catch (Exception ignore) {}
        return false;
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            List<FriendMapping> mappings = this.mySQLService.findFriends(player).orElseThrow();
            Long snowflake = this.cache.put(mappings);
            this.players.put(player, snowflake);

            return Optional.of(mappings);
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                 // TODO actually handle duplicates properly instead of pretending they don't exist
            } catch (SQLIntegrityConstraintViolationException ignore) {}

            try {
                this.getPlayersCacheEntry(player1).orElseThrow().add(mapping);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                this.getPlayersCacheEntry(player2).orElseThrow().add(mapping);
            } catch (Exception e) {
                e.printStackTrace();
            }

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
        try {
            this.getPlayersCacheEntry(player1).orElseThrow().remove(mapping);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            this.getPlayersCacheEntry(player2).orElseThrow().remove(mapping);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void kill() {
        this.cache.getAll().forEach(List::clear);
        this.cache.empty();
        this.players.clear();
    }
}
