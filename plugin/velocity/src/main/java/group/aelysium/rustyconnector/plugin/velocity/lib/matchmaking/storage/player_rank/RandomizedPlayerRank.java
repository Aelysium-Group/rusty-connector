package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IRandomizedPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

public class RandomizedPlayerRank implements IRandomizedPlayerRank {
    public Double rank() { return 0.0; }

    public IScoreCard.IRankSchema.Type<Class<IRandomizedPlayerRank>> type() {
        return IScoreCard.IRankSchema.RANDOMIZED;
    }

    @Override
    public <TMySQLStorage extends IMySQLStorageService> void markWin(TMySQLStorage storage) {}

    @Override
    public <TMySQLStorage extends IMySQLStorageService> void markLoss(TMySQLStorage storage) {}
}
