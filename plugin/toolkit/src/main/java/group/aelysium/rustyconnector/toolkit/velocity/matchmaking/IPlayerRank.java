package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import group.aelysium.rustyconnector.toolkit.core.JSONParseable;

import java.util.List;

public interface IPlayerRank extends JSONParseable {
    /**
     * Compiles the attributes of this rankholder and returns it's rank.
     */
    double rank();

    /**
     * Returns the string name of the ranking schema.
     */
    String schemaName();

    /**
     * Returns the computer used to compute new ranks.
     */
    IComputor computor();

    interface GameOutcome {
        int WON = 1;
        int TIED = 0;
        int LOST = -1;
    }

    interface IComputor {
        /**
         * Compute the new rank of all the winners and losers.
         * @param winners The winners.
         * @param losers The losers.
         */
        void compute(List<IMatchPlayer> winners, List<IMatchPlayer> losers, IMatchmaker matchmaker, ISession session);

        /**
         * Compute a tie between all players.
         */
        void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session);
    }
}
