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

public class WinRatePlayerRank implements IVelocityPlayerRank {
    public static String schema() {
        return "WIN_RATE";
    }

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

    protected void addWin() {
        this.wins++;
    }

    protected void addLoss() {
        this.losses++;
    }

    protected void addTie() {
        this.ties++;
    }

    public double rank() {
        int games = wins + losses + ties;
        if (games == 0) return 0;
        return (double) wins / games;
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
        object.add("ties", new JsonPrimitive(this.ties));
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
                ((WinRatePlayerRank) w.gameRank()).addWin();
                storage.set(w);
            });
            losers.forEach(l->{
                ((WinRatePlayerRank) l.gameRank()).addLoss();
                storage.set(l);
            });
        }

        @Override
        public void computeTie(List<IMatchPlayer> players, IMatchmaker matchmaker, ISession session) {
            IDatabase.PlayerRanks storage = matchmaker.storage();
            players.forEach(p->{
                ((WinRatePlayerRank) p.gameRank()).addTie();
                storage.set(p);
            });
        }
    }
}
