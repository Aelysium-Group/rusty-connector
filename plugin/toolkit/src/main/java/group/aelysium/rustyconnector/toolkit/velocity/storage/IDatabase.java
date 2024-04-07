package group.aelysium.rustyconnector.toolkit.velocity.storage;

import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDatabase {
    Players players();
    FriendLinks friends();
    PlayerRanks ranks();

    interface Players {
        /**
         * Sets the player in this database.
         * If the player hasn't been stored before, it will be stored.
         * If the player already exists, their data will be updated.
         * @param player The player to set.
         */
        void set(IPlayer player);

        /**
         * Gets a player from the database.
         * @param uuid The uuid to search for.
         * @return The player if they exist. Empty otherwise.
         */
        Optional<IPlayer> get(UUID uuid);

        /**
         * Gets a player from the database.
         * @param username The username to search for.
         * @return The player if they exist. Empty otherwise.
         */
        Optional<IPlayer> get(String username);
    }
    interface FriendLinks {
        /**
         * Sets two players as friends.
         * The order that the players are provided, doesn't matter.
         * @param player1 One of the players in the friendship.
         * @param player2 The other player in the friendship.
         */
        void set(IPlayer player1, IPlayer player2);

        /**
         * Gets all the friends for a player.
         * @param player The player to fetch friends for.
         * @return A list of the player's friends. Or Empty if there was an issue.
         */
        Optional<List<IPlayer>> get(IPlayer player);

        /**
         * Deletes the friendship between two players.
         * The order that the players are provided, doesn't matter.
         * @param player1 One of the players in the friendship.
         * @param player2 The other player in the friendship.
         */
        void delete(IPlayer player1, IPlayer player2);

        /**
         * Checks if the two players are friends.
         * The other that the players are provided, doesn't matter.
         * @param player1 One of the players in the potential friendship.
         * @param player2 The other player in the potential friendship.
         * @return `true` is they are friends, `false` otherwise. Will return Empty if there was an issue running the query.
         */
        Optional<Boolean> contains(IPlayer player1, IPlayer player2);
    }
    interface ServerResidences {
        /**
         * Registers a new residence for the player.
         * The residence will expire after whatever expiration is defined in the family's config.
         * @param family The Static Family which houses the residence.
         * @param mcLoader The MCLoader which is the residence.
         * @param player The player.
         */
        void set(IStaticFamily family, IMCLoader mcLoader, IPlayer player);

        /**
         * Deletes all residences listed for a family id.
         * @param familyId The family id to delete residences for.
         */
        void delete(String familyId);

        /**
         * Deletes a player's residence within a family.
         * @param familyId The family id to delete the residence for.
         * @param player The player to delete the residence for.
         */
        void delete(String familyId, IPlayer player);

        /**
         * Gets the residence from a family.
         * @param family The family to fetch the residence from.
         * @param player The player to fetch teh residence for.
         * @return A residence if it exists. An Empty otherwise.
         */
        Optional<IServerResidence.MCLoaderEntry> get(IStaticFamily family, IPlayer player);

        /**
         * Deletes any and all residences which are expired across the entire database.
         */
        void purgeExpired();

        /**
         * Refreshes all residences within the family to have a new expiration.
         * The new expiration is derived from whatever is defined in the family's config.
         * @param family The family to update the residences of.
         */
        void refreshExpirations(IStaticFamily family);
    }
    interface PlayerRanks {
        /**
         * Deletes all ranks associated with a Game ID.
         * @param gameId The Game ID to delete ranks for.
         */
        void deleteGame(String gameId);

        /**
         * Deletes all of a players ranks across all Game IDs.
         * @param player The player to delete all ranks for.
         */
        void delete(IPlayer player);

        /**
         * Deletes a specific rank from a player.
         * @param player The player whose rank is to be deleted.
         * @param gameId The Game's ID to delete the rank for.
         */
        void delete(IPlayer player, String gameId);

        /**
         * Stores a players rank for a specific gamemode.
         * @param player The MatchPlayer to store.
         */
        void set(IMatchPlayer<IPlayerRank> player);

        /**
         * Gets a player's rank from a specific game.
         * @param player The player whose rank is being fetched.
         * @param gameId The Game's ID to fetch the rank from.
         * @return The players rank. Or Empty if there is no rank, or there was an issue getting it.
         */
        Optional<IPlayerRank> get(IPlayer player, String gameId);
    }
}