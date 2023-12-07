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

public interface IFamily<TMCLoader extends IMCLoader, TPlayer extends IPlayer, TLoadBalancer extends ILoadBalancer<TMCLoader>> {
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
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    IWhitelist whitelist();

    /**
     * Get all players in the family.
     * @return A list of players.
     */
    List<Player> players();

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
     * Returns this family's {@link ILoadBalancer}.
     * @return {@link ILoadBalancer}
     */
    TLoadBalancer loadBalancer();

    /**
     * Fetches a reference to the parent of this family.
     * The parent of this family should always be either another family, or the root family.
     * If this family is the root family, this method will always return `null`.
     * @return {@link WeakReference <IBaseFamily>}
     */
    IFamily<TMCLoader, TPlayer, TLoadBalancer> parent();

    /**
     * Returns the metadata for this family.
     * @return {@link Metadata}
     */
    Metadata metadata();

    record Settings<TPlayer extends IPlayer, TMCLoader extends IMCLoader, TFamily extends IFamily<TMCLoader, TPlayer, TLoadBalancer>, TLoadBalancer extends ILoadBalancer<TMCLoader>, TWhitelist extends IWhitelist>
            (Component displayName, TLoadBalancer loadBalancer, Reference<TMCLoader, TPlayer, TLoadBalancer, TFamily> parent, group.aelysium.rustyconnector.toolkit.velocity.util.Reference<TWhitelist, String> whitelist) {}


    abstract class Reference<TMCLoader extends IMCLoader, TPlayer extends IPlayer, TLoadBalancer extends ILoadBalancer<TMCLoader>, TFamily extends IFamily<TMCLoader, TPlayer, TLoadBalancer>> extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<TFamily, String> {
        public Reference(String referencer) {
            super(referencer);
        }

        /**
         * Gets the family referenced.
         * If the family could not be found, this will throw an exception.
         * This method is equivalent to calling {@link #get(boolean) .get(false)}
         * @return {@link TFamily}
         * @throws java.util.NoSuchElementException If the owner of this reference can't be found.
         */
        public abstract TFamily get();

        /**
         * Gets the family referenced.
         * If no family could be found and {@param fetchRoot} is disabled, will throw an exception.
         * If {@param fetchRoot} is enabled and the family isn't found, will return the root family instead.
         * @param fetchRoot Should the root family be returned if the parent family can't be found?
         * @return {@link TFamily}
         * @throws java.util.NoSuchElementException If {@param fetchRoot} is disabled and the family can't be found.
         */
        public abstract TFamily get(boolean fetchRoot);
    }
}
