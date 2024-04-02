package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage;

public interface IPlayerRank {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     */
    double rank();

    RankSchema type();

    void markWin();
    void markLoss();

    enum RankSchema {
        RANDOMIZED,
        WIN_LOSS,
        WIN_RATE
    }
}
