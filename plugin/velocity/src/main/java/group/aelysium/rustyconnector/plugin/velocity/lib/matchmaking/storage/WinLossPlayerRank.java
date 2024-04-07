package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class WinLossPlayerRank implements IPlayerRank {
    protected int wins;
    protected int losses;

    public WinLossPlayerRank(int wins, int losses) {
        this.wins = wins;
        this.losses = losses;
    }
    public WinLossPlayerRank() {
        this(0, 0);
    }

    public void markWin() {
        this.wins = this.wins + 1;
    }

    public void markLoss() {
        this.losses = this.losses + 1;
    }

    public double rank() {
        return (double) wins / losses;
    }

    public String schemaName() {
        return "WIN_LOSS";
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
