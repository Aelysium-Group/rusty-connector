package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;

public interface IRandomizedPlayerRank extends IPlayerRank<Double> {
    Double rank();

    IScoreCard.IRankSchema.Type<Class<IRandomizedPlayerRank>> type();
}
