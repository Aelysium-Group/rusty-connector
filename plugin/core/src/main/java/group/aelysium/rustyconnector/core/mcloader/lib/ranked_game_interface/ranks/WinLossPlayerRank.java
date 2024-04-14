package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.ranks;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IDatabase;

import java.util.List;

public class WinLossPlayerRank implements IPlayerRank {
    public static String schema() {
        return "WIN_LOSS";
    }

    protected int wins;
    protected int losses;

    public WinLossPlayerRank(int wins, int losses) {
        this.wins = wins;
        this.losses = losses;
    }
    public WinLossPlayerRank() {
        this(0, 0);
    }

    public double rank() {
        if (losses == 0) {
            if (wins == 0) return 0;
            else return 1;
        }

        return (double) wins / losses;
    }

    public String schemaName() {
        return schema();
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
