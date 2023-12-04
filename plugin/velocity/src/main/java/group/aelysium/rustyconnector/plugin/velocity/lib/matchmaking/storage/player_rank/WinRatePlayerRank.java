package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IWinRatePlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

import static one.microstream.math.XMath.round;

public class WinRatePlayerRank implements IWinRatePlayerRank {
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
        int games = wins + losses;

        return round((double) wins / games, 4);
    }

    public IScoreCard.IRankSchema.Type<Class<IWinRatePlayerRank>> type() {
        return IScoreCard.IRankSchema.WIN_RATE;
    }
}
