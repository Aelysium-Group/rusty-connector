package group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors;

import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Module containing valid Storage queries that can be used to access database information.
 */
public abstract class StorageReactor {
    public abstract void updateExpirations(String familyName, LiquidTimestamp newExpiration);
    public abstract void purgeExpiredServerResidences(String familyName);
    public abstract Optional<IServerResidence.MCLoaderEntry> fetchServerResidence(String familyName, UUID player);
    public abstract void deleteServerResidence(String familyName, UUID player);
    public abstract void deleteServerResidences(String familyName);
    public abstract void saveServerResidence(String familyName, UUID mcloader, UUID player, LiquidTimestamp expiration);

    /**
     * Deletes the friend link between two players.
     * These two players will no longer be friends after this is run.
     * It doesn't matter what order the UUIDs are provided,
     * they will be sorted before the request is made.
     * @param player1 The first player's UUID.
     * @param player2 The second player's UUID.
     */
    public abstract void deleteFriendLink(UUID player1, UUID player2);

    /**
     * Checks if two players are friends.
     * It doesn't matter what order the UUIDs are provided,
     * they will be sorted before the request is made.
     * @param player1 The first player's UUID.
     * @param player2 The second player's UUID.
     * @return An empty Optional if there was an issue with the request. An Optional boolean if the two players are or are not friends.
     */
    public abstract Optional<Boolean> areFriends(UUID player1, UUID player2);

    /**
     * Fetches all the friends of the passed player.
     * If the optional is empty it means no friends could be found or there was another issue.
     * If the optional is not empty but the List is empty, it simply means the player has no friends.
     * @param player The player to fetch friends from.
     * @return An empty Optional if there was an issue with the request. A List of the player's friends otherwise.
     */
    public abstract Optional<List<IPlayer>> fetchFriends(UUID player);

    /**
     * Saves a new friend link for the two players.
     * This method will mark both players as friends.
     * It doesn't matter what order the UUIDs are provided,
     * they will be sorted before getting stored.
     * @param player1 The first player in the link.
     * @param player2 The second player in the link.
     */
    public abstract void saveFriendLink(UUID player1, UUID player2);


    /**
     * Fetches a player based on their UUID.
     * @param uuid The uuid to fetch.
     */
    public abstract Optional<IPlayer> fetchPlayer(UUID uuid);

    /**
     * Fetches a player based on their username.
     * @param username The username to fetch.
     */
    public abstract Optional<IPlayer> fetchPlayer(String username);

    /**
     * Saves a player.
     * If the player is already saved, this will update it.
     * @param player The player to save or update.
     */
    public abstract void savePlayer(IPlayer player);

    /**
     * Deletes all entries associated with a game id.
     * @param gameId The game ID to delete.
     */
    public abstract void deleteGame(String gameId);

    /**
     * Deletes a player's rank.
     * @param key The rank key to delete.
     */
    public abstract void deleteRank(IPlayer.RankKey key);

    /**
     * Saves a player's rank.
     * If their rank is already saved, this will replace it.
     * @param key The key to save the rank for.
     * @param rank The rank.
     */
    public abstract void saveRank(IPlayer.RankKey key, IPlayerRank rank);

    /**
     * Fetches a player's rank.
     * @param key The key to fetch.
     * @return The player's rank.
     */
    public abstract Optional<IPlayerRank> fetchRank(IPlayer.RankKey key);
}
