package group.aelysium.rustyconnector.toolkit.velocity.family;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.whitelist.IWhitelist;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IFamily extends PlayerConnectable {
    String id();
    String displayName();

    /**
     * Get a server that is a part of the family.
     * @param uuid The uuid of the server to get.
     * @return A found server or `null` if there's no match.
     */
    IMCLoader findServer(@NotNull UUID uuid);

    /**
     * Add a server to the family.
     * @param server The server to add.
     */
    void addServer(IMCLoader server);

    /**
     * Remove a server from this family.
     * @param server The server to remove.
     */
    void removeServer(IMCLoader server);

    /**
     * Get the whitelist for this family, or `null` if there isn't one.
     * @return The whitelist or `null` if there isn't one.
     */
    IWhitelist whitelist();

    /**
     * Get all players in the family.
     * @return A list of players.
     */
    List<com.velocitypowered.api.proxy.Player> players();

    /**
     * Get all players in the family up to approximately `max`.
     * @param max The approximate max number of players to return.
     * @return A list of players.
     */
    List<com.velocitypowered.api.proxy.Player> players(int max);

    List<? extends IMCLoader> registeredServers();

    boolean containsServer(UUID uuid);

    /**
     * Gets the aggregate player count across all servers in this family
     * @return A player count
     */
    long playerCount();

    /**
     * Returns this family's {@link ILoadBalancer}.
     * @return {@link ILoadBalancer}
     */
    ILoadBalancer<IMCLoader> loadBalancer();

    /**
     * Fetches a reference to the parent of this family.
     * The parent of this family should always be either another family, or the root family.
     * If this family is the root family, this method will always return `null`.
     * @return {@link IFamily}
     */
    IFamily parent();

    /**
     * Returns the metadata for this family.
     * @return {@link Metadata}
     */
    Metadata metadata();

    /**
     * Causes the family to balance itself based on it's {@link ILoadBalancer}.
     * If your network has lots of servers in its families, using this method foolishly will greatly impact performance.
     * Be sure to read how RC uses this method before implementing it yourself.
     */
    void balance();

    /**
     * Attempts to fetch a server that a player should theoretically be able to connect to.
     */
    Optional<IMCLoader> smartFetch();

    record Settings(
            String displayName,
            ILoadBalancer<IMCLoader> loadBalancer,
            Reference parent,
            group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IWhitelist, String> whitelist,
            Connector.Core connector) {}

    class Connector {
        public static abstract class Core implements PlayerConnectable {
            protected final ILoadBalancer<IMCLoader> loadBalancer;
            protected IWhitelist whitelist;

            protected Core(@NotNull ILoadBalancer<IMCLoader> loadBalancer, group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IWhitelist, String> whitelist) {
                this.loadBalancer = loadBalancer;
                try {
                    this.whitelist = whitelist.get();
                } catch (Exception ignore) {}
            }

            protected boolean whitelisted(IPlayer player) throws RuntimeException {
                if(this.whitelist == null) return true;

                return whitelist.validate(player);
            }

            @Override
            public void leave(IPlayer player) {}

            public Optional<IMCLoader> fetchAny() {
                return loadBalancer.current();
            }

            protected boolean validateLoadBalancer() {
                return this.loadBalancer.size(false) > 0;
            }
        }
    }

    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IFamily, String> {
        private boolean rootFamily = false;

        public Reference(String name) {
            super(name);
        }
        protected Reference() {
            super(null);
            this.rootFamily = true;
        }

        public <TFamily extends IFamily> TFamily get() {
            VelocityTinder tinder = RustyConnector.Toolkit.proxy().orElseThrow();

            if(rootFamily) return (TFamily) tinder.services().family().rootFamily();
            return (TFamily) tinder.services().family().find(this.referencer).orElseThrow();
        }

        public <TFamily extends IFamily> TFamily get(boolean fetchRoot) {
            VelocityTinder tinder = RustyConnector.Toolkit.proxy().orElseThrow();

            if(rootFamily) return (TFamily) tinder.services().family().rootFamily();
            if(fetchRoot)
                try {
                    return (TFamily) tinder.services().family().find(this.referencer).orElseThrow();
                } catch (Exception ignore) {
                    return (TFamily) tinder.services().family().rootFamily();
                }
            else return (TFamily) tinder.services().family().find(this.referencer).orElseThrow();
        }

        public static Reference rootFamily() {
            return new Reference();
        }
    }
}
