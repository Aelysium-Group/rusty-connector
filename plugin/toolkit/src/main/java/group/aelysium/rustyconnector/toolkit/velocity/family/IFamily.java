package group.aelysium.rustyconnector.toolkit.velocity.family;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.List;

public interface IFamily<TMCLoader extends IMCLoader, TPlayer extends IPlayer> {
    String id();
    Component displayName();

    /**
     * Get a server that is a part of the family.
     * @param serverInfo The info matching the server to get.
     * @return A found server or `null` if there's no match.
     */
    TMCLoader findServer(@NotNull ServerInfo serverInfo);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    void addServer(TMCLoader server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    void removeServer(TMCLoader server);

    /**
     * Get the whitelist for this family.
     * @return {@link IWhitelist}
     * @throws java.util.NoSuchElementException If no whitelist exists for this fmaily.
     */
    IWhitelist whitelist();

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    List<Player> players(int max);

    List<TMCLoader> registeredServers();

    boolean containsServer(ServerInfo serverInfo);

    /**
     * Method added for convenience.
     * Any implementation of this interface should perform some form of operation when connect is called.
     * @param player The player to ultimately connect to the family
     * @return The server that the player was connected to.
     */
    TMCLoader connect(TPlayer player);

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    long playerCount();

    /**
     * Gets the number of {@link IMCLoader PlayerServers} that are registered to this family.
     * This method counts both locked and unlocked servers.
     * To get the count of either locked or unlocked use: {@link ILoadBalancer#size(boolean) loadBalancer().size(true)} or {@link ILoadBalancer#size(boolean) loadBalancer().size(false)}
     * @return {@link Long}
     */
    long serverCount();

    /**
     * Returns this family's {@link ILoadBalancer}.
     * @return {@link ILoadBalancer}
     */
    ILoadBalancer<TMCLoader> loadBalancer();

    /**
     * Fetches a reference to the parent of this family.
     * The parent of this family should always be either another family, or the root family.
     * If this family is the root family, this method will always return `null`.
     * @return {@link WeakReference <IBaseFamily>}
     */
    IFamily<TMCLoader, TPlayer> parent();

    /**
     * Returns the metadata for this family.
     * @return
     */
    Metadata metadata();
}
