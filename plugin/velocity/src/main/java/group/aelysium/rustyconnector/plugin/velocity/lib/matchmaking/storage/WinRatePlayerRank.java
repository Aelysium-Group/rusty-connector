package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class WinRatePlayerRank implements IPlayerRank {
    protected int wins;
    protected int losses;

    public WinRatePlayerRank(int wins, int losses) {
        this.wins = wins;
        this.losses = losses;
    }
    public WinRatePlayerRank() {
        this(0, 0);
    }

    public void markWin() {
        this.wins = this.wins + 1;
    }

    public void markLoss() {
        this.losses = this.losses + 1;
    }

    public double rank() {
        try {
            int games = wins + losses;
            return (double) wins / games;
        } catch (ArithmeticException ignore) {}
        return 0;
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
        return object;
    }
}
