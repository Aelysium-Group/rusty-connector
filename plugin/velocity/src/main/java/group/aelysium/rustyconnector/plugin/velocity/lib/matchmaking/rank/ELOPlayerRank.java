package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;

import java.util.List;

public class ELOPlayerRank implements IPlayerRank {
    private double elo = 1200;

    public ELOPlayerRank() {}
    public ELOPlayerRank(double elo) {
        this.elo = elo;
    }

    @Override
    public double rank() {
        return elo;
    }

    public void setRank(double elo) {
        this.elo = elo;
    }

    public String schemaName() {
        return "ELO";
    }

    public IComputor computor() {
        return Computer.New();
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("elo", new JsonPrimitive(this.elo));
        return object;
    }

    public static class Computer implements IPlayerRank.IComputor {
        private static final double K_FACTOR = 32.0;
        private static final double INITIAL_ELO = 1200.0;
        private static final double ELO_FACTOR = 400.0;

        private static final Computer singleton = new Computer();
        public static Computer New() {
            return singleton;
        }
        private Computer() {}


        @Override
        public void compute(List<IMatchPlayer> winners, List<IMatchPlayer> losers, IMatchmaker matchmaker, ISession session) {
            double averageWinnersRank = averageRank(winners);
            double averageLosersRank = averageRank(losers);

            double expectedWinners = expected(averageWinnersRank, averageLosersRank);
            double expectedLosers = expected(averageLosersRank, averageWinnersRank);

            winners.forEach(winner -> adjustRank(winner, 1, expectedWinners));
            losers.forEach(loser -> adjustRank(loser, 0, expectedLosers));
        }

        @Override
        public void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session) {
            double averageRank = averageRank(players);

            players.forEach(player -> {
                double expected = expected(averageRank, averageRank);
                adjustRank(player, 0.5, expected);
            });
        }

        private void adjustRank(IMatchPlayer player, double outcome, double expected) {
            double oldRank = player.gameRank().rank();
            double newRank = oldRank + K_FACTOR * (outcome - expected);
            ((ELOPlayerRank) player.gameRank()).setRank(newRank);
        }

        private double expected(double ratingA, double ratingB) {
            return 1 / (1 + Math.pow(10, (ratingB - ratingA) / ELO_FACTOR));
        }

        protected double averageRank(List<IMatchPlayer> players) {
            return players.stream().mapToDouble(p -> p.gameRank().rank()).average().orElse(INITIAL_ELO);
        }
    }
}

