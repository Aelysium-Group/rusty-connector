package group.aelysium.rustyconnector.toolkit.proxy.family.mcloader;

import group.aelysium.rustyconnector.toolkit.RC;
import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.Permission;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MCLoader implements ISortable, IPlayer.Connectable {
    private final UUID uuid;
    private final String displayName;
    private final String podName;
    private final InetSocketAddress address;
    private final Particle.Flux<Family> family;
    private Object raw = null;
    private AtomicLong playerCount = new AtomicLong(0);
    private int weight;
    private int softPlayerCap;
    private int hardPlayerCap;
    private AtomicInteger timeout;

    public MCLoader(
            @NotNull UUID uuid,
            @NotNull InetSocketAddress address,
            @Nullable String podName,
            @Nullable String displayName,
            @NotNull Particle.Flux<Family> family,
            int softPlayerCap,
            int hardPlayerCap,
            int weight,
            int timeout
    ) {
        this.uuid = uuid;
        this.address = address;
        this.podName = podName;
        this.displayName = displayName;
        this.family = family;

        this.weight = Math.max(weight, 0);

        this.softPlayerCap = softPlayerCap;
        this.hardPlayerCap = hardPlayerCap;

        // Soft player cap MUST be at most the same value as hard player cap.
        if(this.softPlayerCap > this.hardPlayerCap) this.softPlayerCap = this.hardPlayerCap;

        this.timeout = new AtomicInteger(timeout);
    }

    /**
     * Checks if the mcloader is stale.
     * @return {@link Boolean}
     */
    public boolean stale() {
        return this.timeout.get() <= 0;
    }

    /**
     * Set's the mcloader's new timeout.
     * @param newTimeout The new timeout.
     */
    public void setTimeout(int newTimeout) {
        if(newTimeout < 0) throw new IndexOutOfBoundsException("New timeout must be at least 0!");
        this.timeout.set(newTimeout);
    }

    /**
     * The {@link UUID} of this mcloader.
     * This {@link UUID} will always be different between servers.
     * If this server unregisters and then re-registers into the proxy, this ID will be different.
     * @return {@link UUID}
     */
    public @NotNull UUID uuid() {
        return this.uuid;
    }

    /**
     * Convenience method to return the MCLoader's display name if it exists.
     * If none exists, it will return the MCLoader's UUID in string format.
     */
    public @NotNull String uuidOrDisplayName() {
        if(displayName == null) return this.uuid.toString();
        return this.displayName;
    }

    /**
     * Gets this mcloader's pod name if it exists.
     * If your RC network isn't a part of a Kubernetes cluster, this will always return an empty optional.
     * @return {@link Optional<String>}
     */
    public @NotNull Optional<String> podName() {
        if(this.podName == null) return Optional.empty();
        return Optional.of(this.podName);
    }

    /**
     * Decrease this mcloader's timeout by 1.
     * Once this value equals 0, this server will become stale and player's won't be able to join it anymore.
     * @param amount The amount to decrease by.
     * @return The new timeout value.
     */
    public int decreaseTimeout(int amount) {
        if(amount > 0) amount = amount * -1;
        this.timeout.addAndGet(amount);
        if(this.timeout.get() < 0) this.timeout.set(0);

        return this.timeout.get();
    }

    /**
     * This MCLoader's address.
     */
    public @NotNull InetSocketAddress address() {
        return this.address;
    }

    /**
     * Gets the raw server that backs this MCLoader.
     */
    public @NotNull Object raw() {
        return this.raw;
    }

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full. `false` otherwise.
     */
    public boolean full() {
        return this.playerCount.get() >= softPlayerCap;
    }

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out. `false` otherwise.
     */
    public boolean maxed() {
        return this.playerCount.get() >= hardPlayerCap;
    }

    /**
     * Set the player count for this server.
     * This number will directly impact whether new players can join this server based on server soft and hard caps.
     * The number set here will be overwritten the next time this server syncs with the proxy.
     * @param playerCount The player count.
     */
    public void setPlayerCount(long playerCount) {
        this.playerCount.set(playerCount);
    }

    /**
     * The soft player cap of this server.
     * If this value is reached by {@link MCLoader#players()}, {@link MCLoader#full()} will evaluate to true.
     * The only way for new players to continue to join this server once it's full is by giving them the soft cap bypass permission.
     * @return {@link Integer}
     */
    public int softPlayerCap() {
        return this.softPlayerCap;
    }

    /**
     * The hard player cap of this server.
     * If this value is reached by {@link MCLoader#players()}, {@link MCLoader#maxed()} will evaluate to true.
     * The only way for new players to continue to join this server once it's maxed is by giving them the hard cap bypass permission.
     *
     * If this value is reached by {@link MCLoader#players()}, it can be assumed that {@link MCLoader#full()} is also true, because this value cannot be less than {@link MCLoader#softPlayerCap()}.
     * @return {@link Integer}
     */
    public int hardPlayerCap() {
        return this.hardPlayerCap;
    }

    /**
     * Get the family this mcloader is associated with.
     * @return {@link Particle.Flux<Family>}
     */
    public @NotNull Particle.Flux<Family> family() {
        return this.family;
    }

    /**
     * Locks the specific server in its respective family so that the load balancer won't return it for players to connect to.
     * If the server is already locked, or doesn't exist in the load balancer, nothing will happen.
     * <br/>
     * This is a convenience method that will fetch this MCLoader's family and run {@link Family#unlockMCLoader(MCLoader)}.
     */
    public void lock() {
        this.family.executeNow(f -> f.lockMCLoader(this));
    }

    /**
     * Unlocks the specific server in its respective family so that the load balancer can return it for players to connect to.
     * If the server is already unlocked, or doesn't exist in the load balancer, nothing will happen.
     * <br/>
     * This is a convenience method that will fetch this MCLoader's family and run {@link Family#lockMCLoader(MCLoader)}.
     */
    void unlock() {
        this.family.executeNow(f -> f.unlockMCLoader(this));
    }

    /**
     * Unregisters the MCLoader from the proxy.
     * <br/>
     * This is a convenience method that will fetch this MCLoader's family and run {@link Family#deleteMCLoader(MCLoader)}.
     */
    void unregister() {
        this.family.executeNow(f -> f.deleteMCLoader(this));
    }

    @Override
    public double sortIndex() {
        return this.playerCount.get();
    }

    @Override
    public int weight() {
        return this.weight;
    }


    private boolean validatePlayerLimits(IPlayer player) throws ExecutionException, InterruptedException, TimeoutException {
        Family family = this.family.access().get(10, TimeUnit.SECONDS);

        if(Permission.validate(
                player,
                "rustyconnector.hardCapBypass",
                Permission.constructNode("rustyconnector.<family id>.hardCapBypass",family.id())
        )) return true; // If the player has permission to bypass hard-player-cap, let them in.

        if(this.maxed()) return false; // If the player count is at hard-player-cap. Boot the player.

        if(Permission.validate(
                player,
                "rustyconnector.softCapBypass",
                Permission.constructNode("rustyconnector.<family id>.softCapBypass",family.id())
        )) return true; // If the player has permission to bypass soft-player-cap, let them in.

        return !this.full();
    }

    @Override
    public IPlayer.Connection.Request connect(IPlayer player) {
        try {
            if (!player.online())
                return IPlayer.Connection.Request.failedRequest(player, Component.text(player.username() + " isn't online."));

            if (!this.validatePlayerLimits(player))
                return IPlayer.Connection.Request.failedRequest(player, Component.text("The server is currently full. Try again later."));

            return RC.P.Adapter().connectServer(this, player);
        } catch (Exception ignore) {}

        return IPlayer.Connection.Request.failedRequest(player, Component.text("Unable to connect you to the server!"));
    }

    @Override
    public long players() {
        return 0;
    }

    @Override
    public String toString() {
        return "["+this.uuidOrDisplayName()+"]("+this.address()+")";
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MCLoader mcLoader = (MCLoader) o;
        return Objects.equals(uuid, mcLoader.uuid());
    }

    public interface Factory {
        @NotNull MCLoader generateMCLoader(
                @NotNull UUID uuid,
                @NotNull InetSocketAddress address,
                @Nullable String podName,
                @Nullable String displayName,
                int softPlayerCap,
                int hardPlayerCap,
                int weight,
                int timeout
        );

        void deleteMCLoader(@NotNull MCLoader mcloader);
        boolean containsMCLoader(@NotNull MCLoader mcloader);

        List<MCLoader> mcloaders();
        List<MCLoader> lockedMCLoaders();
        List<MCLoader> unlockedMCLoaders();

        /**
         * Locks the specific MCLoader.
         * If an MCLoader is locked, player's shouldn't be able to connect to it directly.
         * @param mcloader The MCLoader to lock. If the MCLoader isn't a member of this factory, nothing will happen.
         */
        void lockMCLoader(@NotNull MCLoader mcloader);

        /**
         * Unlocks the specific MCLoader.
         * If an MCLoader is unlocked, player's should be able to connect to it directly.
         * @param mcloader The MCLoader to unlock. If the MCLoader isn't a member of this factory, nothing will happen.
         */
        void unlockMCLoader(@NotNull MCLoader mcloader);

        /**
         * Checks if the specified mcloader is locked or not.
         * @param mcloader The mcloader to check.
         * @return `true` if the mcloader is locked. `false` if the mcloader is unlocked. This method also returns `false` if the MCLoader simply doesn't exist.
         */
        boolean isLocked(@NotNull MCLoader mcloader);
    }
}
