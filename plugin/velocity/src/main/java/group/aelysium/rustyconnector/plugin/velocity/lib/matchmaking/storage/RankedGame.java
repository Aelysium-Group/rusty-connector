package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedGame;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;
import org.eclipse.serializer.collections.lazy.LazyHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A RankedGame is a representation of all variations of a player's rank within a specific gamemode.
 * If, over the time of this game existing, it has ranked players based on both ELO and WIN_RATE, both of those
 * ranks are saved here and can be retrieved.
 */
public class RankedGame implements IRankedGame<Player> {
    protected String name;
    protected IScoreCard.IRankSchema.Type<?> rankingSchema;
    protected Map<UUID, ScoreCard> scorecards = new LazyHashMap<>();

    public RankedGame(String name, IScoreCard.IRankSchema.Type<?> schema) {
        this.name = name;
        this.rankingSchema = schema;
    }

    public String name() {
        return this.name;
    }

    public <TPlayerRank extends IPlayerRank<?>, TMySQLStorage extends IMySQLStorageService> TPlayerRank rankedPlayer(TMySQLStorage storage, UUID uuid) {
        ScoreCard scorecard = this.scorecards.get(uuid);
        if(scorecard == null) {
            ScoreCard newScorecard = new ScoreCard();
            this.scorecards.put(uuid, newScorecard);

            storage.store(this.scorecards);

            scorecard = newScorecard;
        }

        TPlayerRank rank = scorecard.fetch((StorageService) storage, this.rankingSchema);

        return (TPlayerRank) RankedPlayer.from(uuid, rank);
    }

    public <TPlayerRank extends IPlayerRank<?>, TMySQLStorage extends IMySQLStorageService> TPlayerRank playerRank(TMySQLStorage storage, Player player) throws IllegalStateException {
        if(rankingSchema == IScoreCard.IRankSchema.RANDOMIZED) return (TPlayerRank) new RandomizedPlayerRank();

        ScoreCard scorecard = this.scorecards.get(player.uuid());
        if(scorecard == null) {
            ScoreCard fresh = new ScoreCard();

            this.scorecards.put(player.uuid(), fresh);
            storage.store(this.scorecards);

            scorecard = fresh;
        }

        TPlayerRank playerRank = scorecard.fetch((StorageService) storage, this.rankingSchema);
        if(playerRank == null) {
            try {
                TPlayerRank fresh = (TPlayerRank) this.rankingSchema.get().getDeclaredConstructor().newInstance();

                scorecard.store((StorageService) storage, fresh);

                playerRank = fresh;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return playerRank;
    }

    public void quantizeRankSchemas(StorageService storage) {
        for (ScoreCard scorecard : this.scorecards.values()) {
            scorecard.quantize(storage, this.rankingSchema);
        }
    }
}
