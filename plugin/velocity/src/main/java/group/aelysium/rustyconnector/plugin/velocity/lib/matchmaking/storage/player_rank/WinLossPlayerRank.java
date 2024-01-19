package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinLossPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import static org.eclipse.serializer.math.XMath.round;

public class WinLossPlayerRank implements IWinLossPlayerRank {
    protected int wins = 0;
    protected int losses = 0;

    public <TMySQLStorage extends IMySQLStorageService> void markWin(TMySQLStorage storage) {
        this.wins = this.wins + 1;

        storage.store(this);
    }

    public <TMySQLStorage extends IMySQLStorageService> void markLoss(TMySQLStorage storage) {
        this.losses = this.losses + 1;

        storage.store(this);
    }

    public Double rank() {
        return round((double) wins / losses, 2);
    }


    public IScoreCard.RankSchema type() {
        return IScoreCard.RankSchema.WIN_LOSS;
    }
}
