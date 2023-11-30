package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.ScoreCard;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

import static one.microstream.math.XMath.round;

public class RandomizedPlayerRank implements IPlayerRank<Boolean> {
    public Boolean rank() { return false; }

    public ScoreCard.RankSchema.Type<Class<RandomizedPlayerRank>> type() {
        return ScoreCard.RankSchema.RANDOMIZED;
    }
}
