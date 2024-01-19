package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import group.aelysium.rustyconnector.plugin.velocity.event_handlers.EventDispatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.velocity.events.player.FamilyPreJoinEvent;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.Metadata;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public abstract class Family implements IFamily {
    protected final String id;
    protected final Metadata metadata;
    protected final Settings settings;

    protected Family(String id, Settings settings, Metadata metadata) {
        this.id = id;
        this.settings = settings;
        this.metadata = metadata;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.settings.displayName();
    }

    public IMCLoader findServer(@NotNull UUID uuid) {
        // Using MCLoader.Reference should be faster since it uses ServerService#fetch which is backed by HashMap.
        try {
            IMCLoader mcLoader = new IMCLoader.Reference(uuid).get();
            if(mcLoader.family().equals(this)) return mcLoader;
            else return null;
        } catch (Exception ignore) {}

        // If the MCLoader can't be found via MCLoader.Reference, try searching the family itself.
        return this.registeredServers().stream()
                .filter(server -> server.uuid().equals(uuid)
                ).findFirst().orElse(null);
    }

    public void addServer(@NotNull IMCLoader server) {
        this.settings.loadBalancer().add(server);
    }

    public void removeServer(@NotNull IMCLoader server) {
        this.settings.loadBalancer().remove(server);
    }

    public Whitelist whitelist() {
        try { return this.settings.whitelist().get(); } catch (Exception ignore) { return null; }
    }

    public void balance() {
        this.settings.loadBalancer().completeSort();
    }

    public List<com.velocitypowered.api.proxy.Player> players(int max) {
        List<com.velocitypowered.api.proxy.Player> players = new ArrayList<>();

        for (IMCLoader server : this.registeredServers()) {
            if(players.size() > max) break;

            players.addAll(server.registeredServer().getPlayersConnected());
        }

        return players;
    }

    public List<com.velocitypowered.api.proxy.Player> players() {
        List<com.velocitypowered.api.proxy.Player> players = new ArrayList<>();

        for (IMCLoader server : this.registeredServers()) {
            players.addAll(server.registeredServer().getPlayersConnected());
        }

        return players;
    }

    public List<IMCLoader> registeredServers() {
        return this.loadBalancer().servers();
    }
    public boolean containsServer(UUID uuid) {
        try {
            return new IMCLoader.Reference(uuid).get().family().equals(this);
        } catch (Exception ignore) {}

        // If the MCLoader can't be found via MCLoader.Reference, try searching the family itself.
        return this.registeredServers().stream().anyMatch(server -> server.uuid().equals(uuid));
    }

    public long playerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.settings.loadBalancer().servers().forEach(server -> newPlayerCount.addAndGet(server.playerCount()));

        return newPlayerCount.get();
    }

    public ILoadBalancer<IMCLoader> loadBalancer() {
        return this.settings.loadBalancer();
    }

    public Family parent() {
        return this.settings.parent().get(true);
    }

    public Metadata metadata() {
        return this.metadata;
    }

    public Request connect(IPlayer player) {
        EventDispatch.UnSafe.fireAndForget(new FamilyPreJoinEvent(this, player));

        return this.settings.connector().connect(player);
    }

    public Optional<IMCLoader> smartFetch() {
        return this.settings.connector().fetchAny();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Family that = (Family) o;
        return Objects.equals(id, that.id);
    }
}
