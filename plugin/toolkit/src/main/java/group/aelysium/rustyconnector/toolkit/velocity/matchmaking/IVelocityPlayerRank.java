package group.aelysium.rustyconnector.toolkit.velocity.matchmaking;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface IVelocityPlayerRank extends group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank {
    /**
     * Returns the computer used to compute new ranks.
     */
    @NotNull IComputer computer();

    interface IComputer {
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
