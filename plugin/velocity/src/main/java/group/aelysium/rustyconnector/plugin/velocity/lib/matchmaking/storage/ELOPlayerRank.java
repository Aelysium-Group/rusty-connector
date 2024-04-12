package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class EloPlayerRank implements IPlayerRank {
    private double wins;
    private double losses;
    private double ties;

    // Constants for ELO calculation
    private static final double INITIAL_ELO = 1000.0;
    private static final double ELO_FACTOR = 400.0;
    private static final double BASE_WIN_PROBABILITY = 0.5;

    public EloPlayerRank() {
        this.wins = 0;
        this.losses = 0;
        this.ties = 0;
    }

    @Override
    public double rank() {
        double totalMatches = wins + losses + ties;

        double winPercentage = 0;
        if(totalMatches != 0) winPercentage = wins / totalMatches;

        return ((winPercentage - BASE_WIN_PROBABILITY) * ELO_FACTOR) + INITIAL_ELO;
    }

    @Override
    public void markWin() {
        wins++;
    }

    @Override
    public void markLoss() {
        losses++;
    }

    @Override
    public void markTie() {
        ties++;
    }

    public String schemaName() {
        return "ELO";
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("wins", new JsonPrimitive(this.wins));
        object.add("losses", new JsonPrimitive(this.losses));
        object.add("ties", new JsonPrimitive(this.ties));
        return object;
    }
}
