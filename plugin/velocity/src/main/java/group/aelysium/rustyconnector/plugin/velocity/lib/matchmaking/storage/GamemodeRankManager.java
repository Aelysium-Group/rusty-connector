package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinLossPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinRatePlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IGamemodeRankManager;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRankProfile;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import org.eclipse.serializer.collections.lazy.LazyHashMap;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A RankedGame is a representation of all variations of a player's rank within a specific gamemode.
 * If, over the time of this game existing, it has ranked players based on both ELO and WIN_RATE, both of those
 * ranks are saved here and can be retrieved.
 */
public class GamemodeRankManager implements IGamemodeRankManager {
    protected String name;
    protected IScoreCard.RankSchema rankingSchema;
    protected Map<UUID, ScoreCard> scorecards = new LazyHashMap<>();

    public GamemodeRankManager(String name, IScoreCard.RankSchema schema) {
        this.name = name;
        this.rankingSchema = schema;
    }

    public String name() {
        return this.name;
    }

    public IPlayerRankProfile rankedPlayer(IMySQLStorageService storage, UUID uuid, boolean createNew) {
        ScoreCard scorecard = this.scorecards.get(uuid);
        if(scorecard == null)
            if(createNew) {
                ScoreCard newScorecard = new ScoreCard();
                this.scorecards.put(uuid, newScorecard);

                storage.store(this.scorecards);

                scorecard = newScorecard;
            } else return null;

        IPlayerRank<?> rank = scorecard.fetch((StorageService) storage, this.rankingSchema);

        return PlayerRankProfile.from(uuid, rank);
    }

    public IPlayerRank<?> playerRank(IMySQLStorageService storage, IPlayer player) throws IllegalStateException {
        if(rankingSchema == IScoreCard.RankSchema.RANDOMIZED) return new RandomizedPlayerRank();

        ScoreCard scorecard = Optional.ofNullable(this.scorecards.get(player.uuid())).orElseGet(() -> {
            ScoreCard fresh = new ScoreCard();

            this.scorecards.put(player.uuid(), fresh);
            storage.store(this.scorecards);

            return fresh;
        });

        return scorecard.fetch((StorageService) storage, this.rankingSchema); // Should never be null
    }

    public void quantizeRankSchemas(StorageService storage) {
        for (ScoreCard scorecard : this.scorecards.values()) {
            scorecard.quantize(storage, this.rankingSchema);
        }
    }
}
