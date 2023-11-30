package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.ScoreCard;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;

import static one.microstream.math.XMath.round;

public class WinLossPlayerRank implements IPlayerRank<Double> {
    protected int wins = 0;
    protected int losses = 0;

    public void markWin(MySQLStorage storage) {
        this.wins = this.wins + 1;

        storage.store(this);
    }

    public void markLoss(MySQLStorage storage) {
        this.losses = this.losses + 1;

        storage.store(this);
    }

    public Double rank() {
        return round((double) wins / losses, 2);
    }

    public ScoreCard.RankSchema.Type<Class<WinLossPlayerRank>> type() {
        return ScoreCard.RankSchema.WIN_LOSS;
    }
}
