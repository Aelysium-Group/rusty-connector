package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.ScoreCard;

public interface IPlayerRank<T> {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     * @return {@link T}
     */
    T rank();

    ScoreCard.RankSchema.Type<?> type();
}
