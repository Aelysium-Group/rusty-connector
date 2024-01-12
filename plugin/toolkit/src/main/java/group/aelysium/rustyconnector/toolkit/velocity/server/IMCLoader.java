package group.aelysium.rustyconnector.toolkit.velocity.server;

import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PartyConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;

import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public interface IMCLoader extends ISortable, PlayerConnectable, PartyConnectable {
    /**
     * Checks if the {@link IMCLoader} is stale.
     * @return {@link Boolean}
     */
    boolean stale();

    /**
     * Set's the {@link IMCLoader PlayerServer's} new timeout.
     * @param newTimeout The new timeout.
     */
    void setTimeout(int newTimeout);

    /**
     * The {@link UUID} of this {@link IMCLoader}.
     * This {@link UUID} will always be different between servers.
     * If this server unregisters and then re-registers into the proxy, this ID will be different.
     * @return {@link UUID}
     */
    UUID uuid();

    /**
     * Convenience method to return the MCLoader's display name if it exists.
     * If none exists, it will return the MCLoader's UUID in string format.
     */
    String uuidOrDisplayName();

    /**
     * Decrease this {@link IMCLoader PlayerServer's} timeout by 1.
     * Once this value equals 0, this server will become stale and player's won't be able to join it anymore.
     * @param amount The amount to decrease by.
     * @return The new timeout value.
     */
    int decreaseTimeout(int amount);

    /**
     * Gets {@link IMCLoader this server's} address in the form of a string.
     * @return {@link String}
     */
    String address();

    /**
     * Gets {@link IMCLoader this server's} associated {@link RegisteredServer}.
     * {@link RegisteredServer} is Velocity's version of RustyConnector's {@link IMCLoader}.
     * This method give you easy access to this object.
     * @return {@link RegisteredServer}
     */
    RegisteredServer registeredServer();

    /**
     * Gets {@link IMCLoader this server's} associated {@link ServerInfo}.
     * @return {@link ServerInfo}
     */
    ServerInfo serverInfo();

    /**
     * Registers a server to RustyConnector.
     * This method also registers the server into the proxy.
     * Registering a server using this method is exactly equivalent to defining this server's details in `velocity.toml`
     * @param familyName The family to associate the server with.
     * @throws DuplicateRequestException If the server has already been registered to the proxy.
     * @throws InvalidAlgorithmParameterException If the family doesn't exist.
     */
    void register(String familyName) throws Exception;

    /**
     * Unregisters the MCLoader from the proxy.
     * @param removeFromFamily Should the MCLoader also be removed from it's family.
     */
    void unregister(boolean removeFromFamily) throws Exception;

    /**
     * Is the server full? Will return `true` if and only if `soft-player-cap` has been reached or surpassed.
     * @return `true` if the server is full. `false` otherwise.
     */
    boolean full();

    /**
     * Is the server maxed out? Will return `true` if and only if `hard-player-cap` has been reached or surpassed.
     * @return `true` if the server is maxed out. `false` otherwise.
     */
    boolean maxed();

    /**
     * Lazily gets the player count for this server.
     * Depending on sync configurations and how often players connect and disconnect form this server.
     * This number can be off from the actual player count.
     * @return {@link Integer}
     */
    int playerCount();

    /**
     * Set the player count for this server.
     * This number will directly impact whether new players can join this server based on server soft and hard caps.
     * The number set here will be overwritten the next time this server syncs with the proxy.
     * @param playerCount The player count.
     */
    void setPlayerCount(int playerCount);

    /**
     * Gets the sort index of this server.
     * This method is used by the {@link ILoadBalancer} to sort this and other servers in a family.
     * @return {@link Integer}
     */
    double sortIndex();

    /**
     * Gets the weight of this server.
     * This method is used by the {@link ILoadBalancer} to sort this and other servers in a family.
     * @return {@link Integer}
     */
    int weight();

    /**
     * The soft player cap of this server.
     * If this value is reached by {@link IMCLoader#playerCount()}, {@link IMCLoader#full()} will evaluate to true.
     * The only way for new players to continue to join this server once it's full is by giving them the soft cap bypass permission.
     * @return {@link Integer}
     */
    int softPlayerCap();

    /**
     * The hard player cap of this server.
     * If this value is reached by {@link IMCLoader#playerCount()}, {@link IMCLoader#maxed()} will evaluate to true.
     * The only way for new players to continue to join this server once it's maxed is by giving them the hard cap bypass permission.
     *
     * If this value is reached by {@link IMCLoader#playerCount()}, it can be assumed that {@link IMCLoader#full()} is also true, because this value cannot be less than {@link IMCLoader#softPlayerCap()}.
     * @return {@link Integer}
     */
    int hardPlayerCap();

    /**
     * Get the family this server is associated with.
     * @return {@link IFamily}
     * @throws IllegalStateException If the server hasn't been registered yet.
     * @throws NullPointerException If the family associated with this server doesn't exist.
     */
    IFamily family() throws IllegalStateException, NullPointerException;

    /**
     * Locks the specific server in its respective family so that the load balancer won't return it for players to connect to.
     * If the server is already locked, or doesn't exist in the load balancer, nothing will happen.
     */
    void lock();

    /**
     * Unlocks the specific server in its respective family so that the load balancer can return it for players to connect to.
     * If the server is already unlocked, or doesn't exist in the load balancer, nothing will happen.
     */
    void unlock();

    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IMCLoader, UUID> {
        public Reference(UUID uuid) {
            super(uuid);
        }

        public <TMCLoader extends IMCLoader> TMCLoader get() {
            return (TMCLoader) RustyConnector.Toolkit.proxy().orElseThrow().services().server().fetch(this.referencer).orElseThrow();
        }
    }
}
