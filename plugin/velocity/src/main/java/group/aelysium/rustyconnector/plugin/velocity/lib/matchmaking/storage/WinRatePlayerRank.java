package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class WinRatePlayerRank implements IPlayerRank {
    protected int wins;
    protected int losses;
    protected int ties;

    public WinRatePlayerRank(int wins, int losses, int ties) {
        this.wins = wins;
        this.losses = losses;
        this.ties = ties;
    }
    public WinRatePlayerRank() {
        this(0, 0, 0);
    }

    public void markWin() {
        this.wins = this.wins + 1;
    }

    public void markLoss() {
        this.losses = this.losses + 1;
    }

    public void markTie() {
        this.ties = this.ties + 1;
    }

    public double rank() {
        int games = wins + losses + ties;
        if (games == 0) return 0;
        return (double) wins / games;
    }

    public String schemaName() {
        return "WIN_RATE";
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
