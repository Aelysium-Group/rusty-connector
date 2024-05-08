package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.rank;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.storage.IDatabase;

import java.util.List;

public class WinLossPlayerRank implements IVelocityPlayerRank {
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

    protected void addWin() {
        this.wins++;
    }

    protected void addLoss() {
        this.losses++;
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

    public IComputor computor() {
        return Computer.New();
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("schema", new JsonPrimitive(this.schemaName()));
        object.add("wins", new JsonPrimitive(this.wins));
        object.add("losses", new JsonPrimitive(this.losses));
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
            IDatabase.PlayerRanks storage = matchmaker.storage();
            winners.forEach(w->{
                ((WinLossPlayerRank) w.gameRank()).addWin();
                storage.set(w);
            });
            losers.forEach(l->{
                ((WinLossPlayerRank) l.gameRank()).addLoss();
                storage.set(l);
            });
        }

        @Override
        public void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session) {

        }
    }
}
