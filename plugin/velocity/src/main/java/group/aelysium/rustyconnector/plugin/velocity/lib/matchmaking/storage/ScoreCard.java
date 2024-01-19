package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinLossPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinRatePlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard.RankSchema.RANDOMIZED;
import static group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard.RankSchema.WIN_LOSS;

/**
 * ScoreCard is a representation of a player's entire ranked game history.
 * All ranks associated with a player should be able to be fetched using their scorecard.
 */
public class ScoreCard implements IScoreCard<StorageService> {
    protected final Map<RankSchema, IPlayerRank<?>> ranks = new ConcurrentHashMap<>();

    public void store(StorageService storage, IPlayerRank<?> rank) {
        this.ranks.put(rank.type(), rank);

        storage.store(ranks);
    }

    public IPlayerRank<?> fetch(StorageService storage, RankSchema schema) {
        IPlayerRank<?> rank = this.ranks.get(schema);
        if(rank == null) {
            rank = switch (schema) {
                case RANDOMIZED -> new RandomizedPlayerRank();
                case WIN_LOSS -> new WinLossPlayerRank();
                case WIN_RATE -> new WinRatePlayerRank();
            };

            this.ranks.put(schema, rank);
            storage.store(this.ranks);
        }

        return rank;
    }

    public void quantize(StorageService storage, RankSchema schema) {
        try {
            IPlayerRank<?> rank = this.ranks.get(schema);

            this.ranks.clear();
            this.ranks.put(schema, rank);

            storage.store(this.ranks);
        } catch (Exception ignore) {
        }

        throw new IllegalStateException();
    }
}
