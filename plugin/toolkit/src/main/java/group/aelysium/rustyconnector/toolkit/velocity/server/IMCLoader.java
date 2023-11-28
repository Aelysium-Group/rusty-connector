package group.aelysium.rustyconnector.toolkit.velocity.server;


import com.sun.jdi.request.DuplicateRequestException;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;

import java.rmi.ConnectException;
import java.security.InvalidAlgorithmParameterException;
import java.util.UUID;

public interface IMCLoader extends ISortable {
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
    UUID id();

    /**
     * Decrease this {@link IMCLoader PlayerServer's} timeout by 1.
     * Once this value equals 0, this server will become stale and player's won't be able to join it anymore.
     * @return The new timeout value.
     */
    int decreaseTimeout();

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
     * Validates the player against the server's current player count.
     * If the server is full or the player doesn't have permissions to bypass soft and hard player caps. They will be kicked
     * @param player The player to validate
     * @return `true` if the player is able to join. `false` otherwise.
     */
    boolean validatePlayer(Player player);

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
     * Record that a player has left this server.
     * This will reduce the player count on this server by 1.
     */
    void playerLeft();

    /**
     * Record that a player has joined this server.
     * This will increase the player count on this server by 1.
     */
    void playerJoined();

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
     * Connect a player to this server.
     * This method respects {@link IParty parties} and will attempt to connect the entire party to this server if the player is in one and has the proper permissions.
     * @param player The {@link Player} to connect.
     * @return `true` if the connection was successful. `false` otherwise.
     */
    boolean connect(Player player) throws ConnectException;

    /**
     * Set's a connections initial server to the server.
     * This method primarily only exists for compatibility reasons for use with Velocity Events.
     * @param event The connection to set.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    boolean directConnect(PlayerChooseInitialServerEvent event);

    /**
     * Connect a player directly to this server.
     * This method is disrespectful of {@link IParty parties} and makes no attempt to accommodate them.
     * @param player The {@link Player} to connect.
     * @return `true` if the connection was successful. `false` otherwise.
     */
    boolean directConnect(Player player) throws ConnectException;
}
