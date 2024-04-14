package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;

import java.util.List;

public class OpenSkillPlayerRank implements IVelocityPlayerRank {
    // Set higher for more conservative confidence levels. 3 is approx 99.7% certain.
    private static final int RANK_CONFIDENCE = 3;
    public static String schema() {
        return "SIMPLE_OPEN_SKILL";
    }

    private double mu = 0;
    private double sigma = 0;

    public OpenSkillPlayerRank() {}
    public OpenSkillPlayerRank(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    @Override
    public double rank() {
        return mu - RANK_CONFIDENCE * sigma;
    }

    protected void setRank(double mu, double sigma) {
        this.mu = mu;
        this.sigma = sigma;
    }

    public String schemaName() {
        return schema();
    }

    public IComputor computor() {
        return Computer.New();
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("mu", new JsonPrimitive(this.mu));
        object.add("sigma", new JsonPrimitive(this.sigma));
        return object;
    }

    public static class Computer implements IComputor {
        private static final Computer singleton = new Computer();
        public static Computer New() {
            return singleton;
        }
        private Computer() {}

        @Override
        public void compute(List<IMatchPlayer> winners, List<IMatchPlayer> losers, IMatchmaker matchmaker, ISession session) {
            // Simplified skill update logic
            winners.forEach(winner -> updateSkill(winner, true, matchmaker));
            losers.forEach(loser -> updateSkill(loser, false, matchmaker));
        }

        @Override
        public void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session) {
            // In case of a tie, slightly increase mu and decrease sigma for all players
            players.forEach(player -> {
                if (player.gameRank() instanceof OpenSkillPlayerRank) {
                    OpenSkillPlayerRank rank = (OpenSkillPlayerRank) player.gameRank();
                    rank.setRank(rank.mu + 1, Math.max(rank.sigma - 1, 1)); // Example adjustment
                    matchmaker.storage().set(player);
                }
            });
        }

        private void updateSkill(IMatchPlayer player, boolean isWinner, IMatchmaker matchmaker) {
            if (player.gameRank() instanceof OpenSkillPlayerRank) {
                OpenSkillPlayerRank rank = (OpenSkillPlayerRank) player.gameRank();
                double muChange = isWinner ? 25 : -25; // Example skill change values
                double sigmaChange = 5; // Example sigma change to reflect increased certainty in skill level
                rank.setRank(rank.mu + muChange, Math.max(rank.sigma - sigmaChange, 1));

                matchmaker.storage().set(player);
            }
        }
    }
}

