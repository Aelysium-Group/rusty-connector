package group.aelysium.rustyconnector.core.mcloader.lib.ranked_game_interface.ranks;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;

import java.util.List;

public class OpenSkillPlayerRank implements IPlayerRank {
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

    public String schemaName() {
        return schema();
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("mu", new JsonPrimitive(this.mu));
        object.add("sigma", new JsonPrimitive(this.sigma));
        return object;
    }

}

