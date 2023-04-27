package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancer implements group.aelysium.rustyconnector.core.lib.LoadBalancer<PlayerServer> {
    private boolean weighted = false;
    private boolean persistence = false;
    private int attempts = 5;
    protected int index = 0;
    protected List<PlayerServer> items = new ArrayList<>();

    @Override
    public boolean isPersistent() {
        return this.persistence;
    }

    @Override
    public int getAttempts() {
        if(!this.isPersistent()) return 0;
        return this.attempts;
    }

    @Override
    public boolean isWeighted() {
        return this.weighted;
    }

    @Override
    public PlayerServer getCurrent() {
        PlayerServer item;
        if(this.index >= this.size()) {
            item = this.items.get(this.index);
            this.index = 0;
        } else item = this.items.get(this.index);

        assert item != null;

        return item;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    @Override
    public void iterate() {
        this.index += 1;
        if(this.index >= this.items.size()) this.index = 0;
    }

    @Override
    final public void forceIterate() {
        this.index += 1;
        if(this.index >= this.items.size()) this.index = 0;
    }

    @Override
    public void completeSort() {}

    @Override
    public void singleSort() {}

    @Override
    public void add(PlayerServer item) {
        this.items.add(item);
    }

    @Override
    public void remove(PlayerServer item) {
        this.items.remove(item);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public List<PlayerServer> dump() {
        return this.items;
    }

    @Override
    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }

    @Override
    public void setPersistence(boolean persistence, int attempts) {
        this.persistence = persistence;
        this.attempts = attempts;
    }

    @Override
    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    @Override
    public void resetIndex() {
        this.index = 0;
    }
}