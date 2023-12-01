package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;

public interface IPlayerRank<T> {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     * @return {@link T}
     */
    T rank();

    IScoreCard.IRankSchema.Type<?> type();
}
