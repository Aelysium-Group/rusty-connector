package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

public interface IPlayerRank {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     */
    double rank();

    void markWin();
    void markLoss();
}
