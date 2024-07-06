package group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing;

import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.events.family.FamilyRebalanceEvent;
import group.aelysium.rustyconnector.toolkit.proxy.events.family.MCLoaderLockedEvent;
import group.aelysium.rustyconnector.toolkit.proxy.events.family.MCLoaderUnlockedEvent;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.util.LiquidTimestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class LoadBalancer implements MCLoader.Factory, Particle {
    protected final ScheduledExecutorService executor;
    protected boolean weighted;
    protected boolean persistence;
    protected int attempts;
    protected int index = 0;
    protected Vector<MCLoader> unlockedServers = new Vector<>();
    protected Vector<MCLoader> lockedServers = new Vector<>();
    protected Map<UUID, MCLoader> mcloaders = new ConcurrentHashMap<>();
    protected Runnable sorter = () -> {
        try {
            MCLoader p = this.unlockedServers.get(0);
            if(p == null) p = this.lockedServers.get(0);
            RC.P.EventManager().fireEvent(new FamilyRebalanceEvent(p.family()));
        } catch (Exception ignore) {}

        this.completeSort();
    };

    protected LoadBalancer(boolean weighted, boolean persistence, int attempts, @Nullable LiquidTimestamp rebalance) {
        this.weighted = weighted;
        this.persistence = persistence;
        this.attempts = attempts;

        if(rebalance == null) this.executor = null;
        else {
            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.executor.schedule(this.sorter, rebalance.value(), rebalance.unit());
        }
    }

    /**
     * Is the load balancer persistent?
     * @return `true` if the load balancer is persistent. `false` otherwise.
     */
    public boolean persistent() {
        return this.persistence;
    }

    /**
     * Is the load balancer weighted?
     * @return `true` if the load balancer is weighted. `false` otherwise.
     */
    public boolean weighted() {
        return this.weighted;
    }

    /**
     * Get the number of attempts that persistence will make.
     * @return The number of attempts.
     */
    public int attempts() {
        if(!this.persistent()) return 0;
        return this.attempts;
    }

    /**
     * Get the item that the iterator is currently pointing to.
     * Once this returns an item, it will automatically iterate to the next item.
     *
     * @return The item.
     */
    public Optional<MCLoader> current() {
        if(this.unlockedServers.isEmpty()) return Optional.empty();

        MCLoader item;
        if(this.index >= this.unlockedServers.size()) {
            this.index = 0;
            item = this.unlockedServers.get(this.index);
        } else item = this.unlockedServers.get(this.index);

        return Optional.of(item);
    }

    /**
     * Get the index number of the currently selected item.
     * @return The current index.
     */
    public int index() {
        return this.index;
    }

    /**
     * Iterate to the next item.
     * Some conditions might apply causing it to not truly iterate.
     */
    public void iterate() {
        this.index += 1;
        if(this.index >= this.unlockedServers.size()) this.index = 0;
    }

    /**
     * No matter what, iterate to the next item.
     */
    final public void forceIterate() {
        this.index += 1;
        if(this.index >= this.unlockedServers.size()) this.index = 0;
    }

    /**
     * Sort the entire load balancer's contents.
     * Also resets the index to 0.
     */
    public abstract void completeSort();

    /**
     * Sort only one index into a new position.
     * The index chosen is this.index.
     * Also resets the index to 0.
     */
    public abstract void singleSort();

    /**
     * Set the persistence of the load balancer.
     * @param persistence The persistence.
     * @param attempts The number of attempts that persistence will try to connect a player before quiting. This value doesn't matter if persistence is set to `false`
     */
    public void setPersistence(boolean persistence, int attempts) {
        this.persistence = persistence;
        this.attempts = attempts;
    }

    /**
     * Set whether the load balancer is weighted.
     * @param weighted Whether the load balancer is weighted.
     */
    public void setWeighted(boolean weighted) {
        this.weighted = weighted;
    }

    /**
     * Resets the index of the load balancer.
     */
    public void resetIndex() {
        this.index = 0;
    }

    /**
     * Attempt to fetch a "good enough" MCLoader for a potential player connection.
     * @return The MCLoader.
     */
    public Optional<MCLoader> staticFetch() {
        return this.current();
    }

    @Override
    public @NotNull MCLoader generateMCLoader(@NotNull UUID uuid, @NotNull InetSocketAddress address, @Nullable String podName, @Nullable String displayName, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        return null;
    }

    @Override
    public void deleteMCLoader(@NotNull MCLoader mcloader) {
        if(!this.mcloaders.containsKey(mcloader.uuid())) return;
        if(!this.unlockedServers.remove(mcloader))
            this.lockedServers.remove(mcloader);
        this.mcloaders.remove(mcloader.uuid());
    }

    @Override
    public boolean containsMCLoader(@NotNull MCLoader mcloader) {
        return this.mcloaders.containsKey(mcloader.uuid());
    }

    @Override
    public List<MCLoader> mcloaders() {
        return this.mcloaders.values().stream().toList();
    }

    @Override
    public List<MCLoader> lockedMCLoaders() {
        return this.lockedServers.stream().toList();
    }

    @Override
    public List<MCLoader> unlockedMCLoaders() {
        return this.unlockedServers.stream().toList();
    }

    @Override
    public void lockMCLoader(@NotNull MCLoader mcloader) {
        if(!this.unlockedServers.remove(mcloader)) return;
        this.lockedServers.add(mcloader);

        RC.P.EventManager().fireEvent(new MCLoaderLockedEvent(mcloader.family(), mcloader));
    }

    @Override
    public void unlockMCLoader(@NotNull MCLoader mcloader) {
        if(!this.lockedServers.remove(mcloader)) return;
        this.unlockedServers.add(mcloader);

        RC.P.EventManager().fireEvent(new MCLoaderUnlockedEvent(mcloader.family(), mcloader));
    }

    @Override
    public boolean isLocked(@NotNull MCLoader mcloader) {
        return false;
    }

    @Override
    public void close() throws Exception {
        this.mcloaders.clear();
        this.unlockedServers.clear();
        this.lockedServers.clear();
        this.executor.shutdownNow();
    }

    public record Settings(
            String algorithm,
            boolean weighted,
            boolean persistence,
            int attempts,
            LiquidTimestamp rebalance
    ) {}
}
