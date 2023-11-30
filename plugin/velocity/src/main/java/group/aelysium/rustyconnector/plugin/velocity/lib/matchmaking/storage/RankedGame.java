package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * ScoreCard is a representation of a player's entire ranked game history.
 * All ranks associated with a player should be able to be fetched using their score card.
 */
public class RankedGame {
    protected String name;
    protected ScoreCard.RankSchema.Type<?> rankingSchema;
    protected HashMap<UUID, ScoreCard> scorecards = new HashMap<>();

    public RankedGame(String name, ScoreCard.RankSchema.Type<?> schema) {
        this.name = name;
        this.rankingSchema = schema;
    }

    public String name() {
        return this.name;
    }

    public <TPlayerRank extends IPlayerRank<?>> RankedPlayer<TPlayerRank> rankedPlayer(MySQLStorage storage, UUID uuid) {
        ScoreCard scorecard = this.scorecards.get(uuid);
        if(scorecard == null) {
            ScoreCard newScorecard = new ScoreCard();
            this.scorecards.put(uuid, newScorecard);

            storage.store(this.scorecards);

            scorecard = newScorecard;
        }

        TPlayerRank rank = scorecard.fetch(storage, this.rankingSchema);

        return RankedPlayer.from(uuid, rank);
    }

    public <TPlayerRank extends IPlayerRank<?>> TPlayerRank playerRank(MySQLStorage storage, Player player) throws IllegalStateException {
        if(rankingSchema == ScoreCard.RankSchema.RANDOMIZED) return (TPlayerRank) new RandomizedPlayerRank();

        ScoreCard scorecard = this.scorecards.get(player.uuid());
        if(scorecard == null) {
            ScoreCard fresh = new ScoreCard();

            this.scorecards.put(player.uuid(), fresh);
            storage.store(this.scorecards);

            scorecard = fresh;
        }

        TPlayerRank playerRank = scorecard.fetch(storage, this.rankingSchema);
        if(playerRank == null) {
            try {
                TPlayerRank fresh = (TPlayerRank) this.rankingSchema.get().getDeclaredConstructor().newInstance();

                scorecard.store(storage, fresh);

                playerRank = fresh;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return playerRank;
    }
}
