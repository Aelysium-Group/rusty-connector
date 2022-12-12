package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;

import java.util.ArrayList;
import java.util.List;

public class PaperServerLoadBalancer implements LoadBalancer<PaperServer> {
    private boolean persistence = false;
    private int attempts = 5;
    protected int index = 0;
    protected List<PaperServer> items = new ArrayList<>();

    @Override
    public boolean isPersistent() {
        return this.persistence;
    }

    @Override
    public Integer getAttempts() {
        if(!this.isPersistent()) return null;
        return this.attempts;
    }

    @Override
    public PaperServer getCurrent() {
        PaperServer item;
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
    public void iterate() {}

    @Override
    public void add(PaperServer item) {
        this.items.add(item);
    }

    @Override
    public void remove(PaperServer item) {
        this.items.remove(item);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public List<PaperServer> dump() {
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
}