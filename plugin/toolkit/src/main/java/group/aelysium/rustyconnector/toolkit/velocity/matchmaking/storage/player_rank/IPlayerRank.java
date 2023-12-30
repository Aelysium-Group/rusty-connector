package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IMySQLStorageService;

public interface IPlayerRank<T> {
    /**
     * Compiles the attributes of this rankholder and returns its rank.
     * @return {@link T}
     */
    T rank();

    IScoreCard.IRankSchema.Type<?> type();

    <TMySQLStorage extends IMySQLStorageService> void markWin(TMySQLStorage storage);
    <TMySQLStorage extends IMySQLStorageService> void markLoss(TMySQLStorage storage);
}
