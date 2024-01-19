package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IRandomizedPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinLossPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinRatePlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

public interface IScoreCard<TMySQLStorageService extends IMySQLStorageService> {
    /**
     * Stores the specified rank into this scorecard.
     * This method will also store the rank into the remote storage resource.
     * @param storage The storage resource to store in.
     * @param rank The rank to store.
     */
    void store(TMySQLStorageService storage, IPlayerRank<?> rank);

    /**
     * Fetches the player's current rank based on the schema provided.
     * If no rank exists for that schema, will create a new rank entry and return that.
     * @param schema The schema to search for.
     * @return {@link IPlayerRank}
     * @throws IllegalStateException If there was a fatal exception while attempting to get the user's rank.
     */
    IPlayerRank<?> fetch(TMySQLStorageService storage, RankSchema schema);

    enum RankSchema {
        RANDOMIZED,
        WIN_LOSS,
        WIN_RATE
    }
}
