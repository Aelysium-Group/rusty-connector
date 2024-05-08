package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;

import java.util.List;

public class RandomizedPlayerRank implements IVelocityPlayerRank {
    public static String schema() {
        return "RANDOMIZED";
    }
    private static final RandomizedPlayerRank singleton = new RandomizedPlayerRank();

    protected RandomizedPlayerRank() {}
    public static RandomizedPlayerRank New() { return singleton; }

    public double rank() { return 1.0; }

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
        return object;
    }

    public static class Computer implements IComputor {
        private static final Computer singleton = new Computer();
        public static Computer New() {
            return singleton;
        }
        private Computer() {}


        @Override
        public void compute(List<IMatchPlayer> winners, List<IMatchPlayer> losers, IMatchmaker matchmaker, ISession session) {}

        @Override
        public void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session) {}
    }
}
