package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class RandomizedPlayerRank implements IPlayerRank {
    public double rank() { return 0.0; }

    @Override
    public void markWin() {}

    @Override
    public void markLoss() {}
}
