package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.toolkit.velocity.events.family.MCLoaderLockedEvent;
import group.aelysium.rustyconnector.toolkit.velocity.events.family.MCLoaderUnlockedEvent;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.*;

public abstract class LoadBalancer implements ILoadBalancer<IMCLoader> {
    private boolean weighted;
    private boolean persistence;
    private int attempts;
    protected int index = 0;
    protected Vector<IMCLoader> unlockedServers = new Vector<>();
    protected Vector<IMCLoader> lockedServers = new Vector<>();

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

    public Optional<IMCLoader> current() {
        if(this.size(false) == 0) return Optional.empty();

        IMCLoader item;
        if(this.index >= this.size()) {
            this.index = 0;
            item = this.unlockedServers.get(this.index);
        } else item = this.unlockedServers.get(this.index);

        return Optional.of(item);
    }

    public int index() {
        return this.index;
    }

    public void iterate() {
        this.index += 1;
        if(this.index >= this.unlockedServers.size()) this.index = 0;
    }

    final public void forceIterate() {
        this.index += 1;
        if(this.index >= this.unlockedServers.size()) this.index = 0;
    }

    public abstract void completeSort();

    public abstract void singleSort();

    public void add(IMCLoader item) {
        if(this.unlockedServers.contains(item)) return;
        this.unlockedServers.add(item);
    }

    public void remove(IMCLoader item) {
        if(this.unlockedServers.remove(item)) return;
        this.lockedServers.remove(item);
    }

    public int size() {
        return this.unlockedServers.size() + this.lockedServers.size();
    }

    public int size(boolean locked) {
        if(locked) return this.lockedServers.size();
        return this.unlockedServers.size();
    }

    public List<IMCLoader> servers() {
        List<IMCLoader> servers = new ArrayList<>();

        servers.addAll(openServers());
        servers.addAll(lockedServers());

        return servers;
    }
    public List<IMCLoader> openServers() {
        return this.unlockedServers.stream().toList();
    }
    public List<IMCLoader> lockedServers() {
        return this.lockedServers.stream().toList();
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

    public boolean contains(IMCLoader item) {
        return this.unlockedServers.contains(item);
    }

    public void lock(IMCLoader server) {
        if(!this.unlockedServers.remove(server)) return;
        this.lockedServers.add(server);

        EventDispatch.UnSafe.fireAndForget(new MCLoaderLockedEvent(server.family(), server));
    }

    public void unlock(IMCLoader server) {
        if(!this.lockedServers.remove(server)) return;
        this.unlockedServers.add(server);

        EventDispatch.UnSafe.fireAndForget(new MCLoaderUnlockedEvent(server.family(), server));
    }

    public boolean joinable(IMCLoader server) {
        return this.unlockedServers.contains(server);
    }

    public record Settings(boolean weighted, boolean persistence, int attempts) {}
}