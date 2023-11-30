package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;

import java.util.ArrayList;
import java.util.List;

public abstract class LoadBalancer implements ILoadBalancer<MCLoader> {
    private boolean weighted;
    private boolean persistence;
    private int attempts;
    protected int index = 0;
    protected List<MCLoader> items = new ArrayList<>();

    public LoadBalancer(Settings settings) {
        this.weighted = settings.weighted();
        this.persistence = settings.persistence();
        this.attempts = settings.attempts();
    }

    public boolean persistent() {
        return this.persistence;
    }

    public int attempts() {
        if(!this.persistent()) return 0;
        return this.attempts;
    }

    public boolean weighted() {
        return this.weighted;
    }

    public MCLoader current() {
        MCLoader item;
        if(this.index >= this.size()) {
            this.index = 0;
            item = this.items.get(this.index);
        } else item = this.items.get(this.index);

        assert item != null;

        return item;
    }

    public int index() {
        return this.index;
    }

    public void iterate() {
        this.index += 1;
        if(this.index >= this.items.size()) this.index = 0;
    }

    final public void forceIterate() {
        this.index += 1;
        if(this.index >= this.items.size()) this.index = 0;
    }

    public abstract void completeSort();

    public abstract void singleSort();

    public void add(MCLoader item) {
        this.items.add(item);
    }

    public void remove(MCLoader item) {
        this.items.remove(item);
    }

    public int size() {
        return this.items.size();
    }

    public List<MCLoader> dump() {
        return this.items;
    }

    public String toString() {
        return "LoadBalancer (RoundRobin): "+this.size()+" items";
    }

    public void setPersistence(boolean persistence, int attempts) {
        this.persistence = persistence;
        this.attempts = attempts;
    }

    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    public void resetIndex() {
        this.index = 0;
    }

    public boolean contains(MCLoader item) {
        return this.items.contains(item);
    }

    public record Settings(boolean weighted, boolean persistence, int attempts) {}
}