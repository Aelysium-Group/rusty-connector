package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class RandomizedPlayerRank implements IPlayerRank {
    private static RandomizedPlayerRank singleton = new RandomizedPlayerRank();

    private RandomizedPlayerRank() {}
    public static RandomizedPlayerRank New() { return singleton; }

    public double rank() { return 1.0; }

    public String schemaName() {
        return "RANDOMIZED";
    }

    @Override
    public void markWin() {}

    @Override
    public void markLoss() {}

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        return object;
    }
}
