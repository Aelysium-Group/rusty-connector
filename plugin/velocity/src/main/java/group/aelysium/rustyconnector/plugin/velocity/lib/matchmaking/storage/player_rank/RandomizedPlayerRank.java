package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;

public class RandomizedPlayerRank implements IPlayerRank {
    public double rank() { return 0.0; }

    public RankSchema type() {
        return RankSchema.RANDOMIZED;
    }

    @Override
    public void markWin() {}

    @Override
    public void markLoss() {}
}
