package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

import java.util.HashMap;
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

    public IPlayerRank<?> playerRank(MySQLStorage storage, Player player) {
        if(rankingSchema == ScoreCard.RankSchema.RANDOMIZED) return new RandomizedPlayerRank();

        ScoreCard scorecard = this.scorecards.get(player.uuid());
        if(scorecard == null) {
            ScoreCard fresh = new ScoreCard();

            this.scorecards.put(player.uuid(), fresh);
            storage.store(this.scorecards);

            scorecard = fresh;
        }

        IPlayerRank<?> playerRank = scorecard.get(this.rankingSchema).orElse(null);
        if(playerRank == null) {
            try {
                IPlayerRank<?> fresh = this.rankingSchema.get().getDeclaredConstructor().newInstance();

                scorecard.store(storage, fresh);

                playerRank = fresh;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return playerRank;
    }
}
