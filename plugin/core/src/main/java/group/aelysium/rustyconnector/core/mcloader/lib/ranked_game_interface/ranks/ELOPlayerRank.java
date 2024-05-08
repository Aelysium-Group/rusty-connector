package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.ranks;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;

import java.util.List;

public class ELOPlayerRank implements IPlayerRank {
    public static String schema() {
        return "ELO";
    }
    private int elo = 1200;

    public ELOPlayerRank() {}
    public ELOPlayerRank(int elo) {
        this.elo = elo;
    }

    @Override
    public double rank() {
        return elo;
    }

    public String schemaName() {
        return schema();
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("elo", new JsonPrimitive(this.elo));
        return object;
    }
}

