package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.velocity.family.IConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.family.Metadata;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Family implements IFamily<MCLoader, Player> {
    protected final String id;
    protected Metadata metadata;
    protected Settings settings;

    protected Family(String id, Settings settings, Metadata metadata) {
        this.id = id;
        this.settings = settings;
        this.metadata = metadata;
    }

    public String id() {
        return this.id;
    }

    public Component displayName() {
        return this.settings.displayName;
    }

    public MCLoader findServer(@NotNull ServerInfo serverInfo) {
        return this.registeredServers().stream()
                .filter(server -> server.serverInfo().equals(serverInfo)
                ).findFirst().orElse(null);
    }

    public void addServer(MCLoader server) {
        this.settings.loadBalancer.add(server);
    }

    public void removeServer(MCLoader server) {
        this.settings.loadBalancer.remove(server);
    }

    public Whitelist whitelist() {
        return this.settings.whitelist.get();
    }

    public List<com.velocitypowered.api.proxy.Player> players(int max) {
        List<com.velocitypowered.api.proxy.Player> players = new ArrayList<>();

        for (MCLoader server : this.registeredServers()) {
            if(players.size() > max) break;

            players.addAll(server.registeredServer().getPlayersConnected());
        }

        return players;
    }

    public List<MCLoader> registeredServers() {
        List<MCLoader> servers = new ArrayList<>();
        servers.addAll(this.settings.loadBalancer.servers());
        servers.addAll(this.settings.loadBalancer.lockedServers());
        return servers;
    }
    public boolean containsServer(ServerInfo serverInfo) {
        return !(this.findServer(serverInfo) == null);
    }

    public long playerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.settings.loadBalancer.servers().forEach(server -> newPlayerCount.addAndGet(server.playerCount()));

        return newPlayerCount.get();
    }

    public long serverCount() { return this.registeredServers().size(); }

    public LoadBalancer loadBalancer() {
        return this.settings.loadBalancer;
    }

    public IConnectable<MCLoader, Player> parent() {
        return this.settings.parent.get(true);
    }

    public Metadata metadata() {
        return this.metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Family that = (Family) o;
        return Objects.equals(id, that.id);
    }

    public record Settings(Component displayName, LoadBalancer loadBalancer, Family.Reference parent, Whitelist.Reference whitelist) {}

    public static class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<Family, String> {
        private boolean rootFamily = false;

        public Reference(String name) {
            super(name);
        }
        protected Reference() {
            super(null);
            this.rootFamily = true;
        }

        public Family get() {
            if(rootFamily) return (Family) Tinder.get().services().family().rootFamily();
            return (Family) Tinder.get().services().family().find(this.referencer).orElseThrow();
        }

        /**
         * Gets the family referenced.
         * If no family could be found and {@param fetchRoot} is disabled, will throw an exception.
         * If {@param fetchRoot} is enabled and the family isn't found, will return the root family instead.
         * @param fetchRoot Should the root family be returned if the parent family can't be found?
         * @return {@link Family}
         * @throws java.util.NoSuchElementException If {@param fetchRoot} is disabled and the family can't be found.
         */
        public Family get(boolean fetchRoot) {
            if(rootFamily) return (Family) Tinder.get().services().family().rootFamily();
            if(fetchRoot)
                try {
                    return (Family) Tinder.get().services().family().find(this.referencer).orElseThrow();
                } catch (Exception ignore) {
                    return (Family) Tinder.get().services().family().rootFamily();
                }
            else return (Family) Tinder.get().services().family().find(this.referencer).orElseThrow();
        }

        public static Reference rootFamily() {
            return new Reference();
        }
    }

    public static class ConnectableReference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IConnectable<MCLoader, Player>, String> {
        private boolean rootFamily = false;

        public ConnectableReference(String name) {
            super(name);
        }
        protected ConnectableReference() {
            super(null);
            this.rootFamily = true;
        }

        public IConnectable<MCLoader, Player> get() {
            if(rootFamily) return Tinder.get().services().family().rootFamily();
            return Tinder.get().services().family().find(this.referencer).orElseThrow();
        }

        /**
         * Gets the family referenced.
         * If no family could be found and {@param fetchRoot} is disabled, will throw an exception.
         * If {@param fetchRoot} is enabled and the family isn't found, will return the root family instead.
         * @param fetchRoot Should the root family be returned if the parent family can't be found?
         * @return {@link Family}
         * @throws java.util.NoSuchElementException If {@param fetchRoot} is disabled and the family can't be found.
         */
        public IConnectable<MCLoader, Player> get(boolean fetchRoot) {
            if(rootFamily) return Tinder.get().services().family().rootFamily();
            if(fetchRoot)
                try {
                    return Tinder.get().services().family().find(this.referencer).orElseThrow();
                } catch (Exception ignore) {
                    return Tinder.get().services().family().rootFamily();
                }
            else return Tinder.get().services().family().find(this.referencer).orElseThrow();
        }

        public static Reference rootFamily() {
            return new Reference();
        }
    }
}
