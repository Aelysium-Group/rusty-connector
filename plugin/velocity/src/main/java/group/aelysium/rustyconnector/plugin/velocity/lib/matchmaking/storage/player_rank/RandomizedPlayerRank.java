package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IRandomizedPlayerRank;

public class RandomizedPlayerRank implements IRandomizedPlayerRank {
    public Boolean rank() { return false; }

    public IScoreCard.IRankSchema.Type<Class<IRandomizedPlayerRank>> type() {
        return IScoreCard.IRankSchema.RANDOMIZED;
    }

}
