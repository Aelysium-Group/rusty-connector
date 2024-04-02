package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage;

import static org.eclipse.serializer.math.XMath.round;

import org.eclipse.serializer.concurrency.XThreads;
import org.eclipse.serializer.persistence.types.Persister;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;

public class WinLossPlayerRank implements IPlayerRank {
    private transient Persister storage;
    protected int wins = 0;
    protected int losses = 0;

    public void markWin() {
        XThreads.executeSynchronized(()->{
            this.wins = this.wins + 1;

            this.storage.store(this);
        });
    }

    public void markLoss() {
        XThreads.executeSynchronized(()->{
            this.losses = this.losses + 1;

            this.storage.store(this);
        });
    }

    public double rank() {
        return round((double) wins / losses, 2);
    }
}
