package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank;

import static org.eclipse.serializer.math.XMath.round;

import org.eclipse.serializer.concurrency.XThreads;
import org.eclipse.serializer.persistence.types.Persister;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;

public class WinRatePlayerRank implements IPlayerRank {
    private transient Persister storage;
    protected int wins = 0;
    protected int losses = 0;

    public void markWin() {
        XThreads.executeSynchronized(()->{
            this.wins = this.wins + 1;
    
            storage.store(this);
        });
    }

    public void markLoss() {
        XThreads.executeSynchronized(()->{
            this.losses = this.losses + 1;
    
            storage.store(this);
        });
    }

    public double rank() {
        int games = wins + losses;

        return round((double) wins / games, 4);
    }
}
