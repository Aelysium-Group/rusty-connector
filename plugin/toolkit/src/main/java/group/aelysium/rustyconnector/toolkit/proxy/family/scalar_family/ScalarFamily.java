package group.aelysium.rustyconnector.toolkit.proxy.family.scalar_family;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.family.Family;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.MostConnection;
import group.aelysium.rustyconnector.toolkit.proxy.family.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.toolkit.proxy.family.whitelist.Whitelist;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class ScalarFamily extends Family {
    protected final Flux<LoadBalancer> loadBalancer;

    protected ScalarFamily(
            @NotNull String id,
            @Nullable String displayName,
            @Nullable String parent,
            @Nullable Flux<Whitelist> whitelist,
            @NotNull Flux<LoadBalancer> loadBalancer
    ) {
        super(id, displayName, parent, whitelist);
        this.loadBalancer = loadBalancer;
    }

    public Particle.Flux<LoadBalancer> loadBalancer() {
        return this.loadBalancer;
    }

    @Override
    public @NotNull MCLoader generateMCLoader(@NotNull UUID uuid, @NotNull InetSocketAddress address, @Nullable String podName, @Nullable String displayName, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        AtomicReference<MCLoader> mcloader = new AtomicReference<>();
        this.loadBalancer.executeNow(l -> mcloader.set(l.generateMCLoader(uuid, address, podName, displayName, softPlayerCap, hardPlayerCap, weight, timeout)));
        return mcloader.get();

    }

    @Override
    public void deleteMCLoader(@NotNull MCLoader mcloader) {
        this.loadBalancer.executeNow(l -> l.deleteMCLoader(mcloader));
    }

    @Override
    public boolean containsMCLoader(@NotNull MCLoader mcloader) {
        AtomicBoolean value = new AtomicBoolean(false);
        this.loadBalancer.executeNow(l -> value.set(l.containsMCLoader(mcloader)));
        return value.get();
    }

    @Override
    public void lockMCLoader(@NotNull MCLoader mcloader) {
        this.loadBalancer.executeNow(l -> l.lockMCLoader(mcloader));
    }

    @Override
    public void unlockMCLoader(@NotNull MCLoader mcloader) {
        this.loadBalancer.executeNow(l -> l.unlockMCLoader(mcloader));
    }

    @Override
    public List<MCLoader> lockedMCLoaders() {
        AtomicReference<List<MCLoader>> value = new AtomicReference<>(new ArrayList<>());
        this.loadBalancer.executeNow(l -> value.set(l.lockedMCLoaders()));
        return value.get();
    }

    @Override
    public List<MCLoader> unlockedMCLoaders() {
        AtomicReference<List<MCLoader>> value = new AtomicReference<>(new ArrayList<>());
        this.loadBalancer.executeNow(l -> value.set(l.unlockedMCLoaders()));
        return value.get();
    }

    public long players() {
        AtomicLong value = new AtomicLong(0);
        this.loadBalancer().executeNow(l -> {
                l.unlockedMCLoaders().forEach(s -> value.addAndGet(s.players()));
                l.unlockedMCLoaders().forEach(s -> value.addAndGet(s.players()));
            }
        );

        return value.get();
    }

    @Override
    public @NotNull Component details() {
        return null;
    }

    @Override
    public List<MCLoader> mcloaders() {
        AtomicReference<List<MCLoader>> mcloaders = new AtomicReference<>(new ArrayList<>());

        this.loadBalancer.executeNow(l -> mcloaders.set(l.mcloaders()));

        return mcloaders.get();
    }

    @Override
    public boolean isLocked(@NotNull MCLoader mcloader) {
        AtomicBoolean valid = new AtomicBoolean(false);
        this.loadBalancer.executeNow(l -> valid.set(l.isLocked(mcloader)));
        return valid.get();
    }

    @Override
    public IPlayer.Connection.Request connect(IPlayer player) {
        if(this.whitelist != null)
            try {
                Whitelist w = this.whitelist.access().get(10, TimeUnit.SECONDS);
                if(!w.validate(player))
                    return IPlayer.Connection.Request.failedRequest(player, Component.text(w.message()));
            } catch (Exception ignore) {}

        try {
            return this.loadBalancer.access().get(20, TimeUnit.SECONDS).current().orElseThrow().connect(player);
        } catch (Exception ignore) {
            return IPlayer.Connection.Request.failedRequest(player, Component.text("The server you're attempting to access isn't available! Try again later."));
        }
    }

    @Override
    public void close() throws Exception {
        this.loadBalancer.close();
        try {
            assert this.whitelist != null;
            this.whitelist.close();
        } catch (Exception ignore) {}
    }

    public record Settings(
            @NotNull String id,
            @Nullable String displayName,
            @Nullable String parent,
            @Nullable Whitelist.Settings whitelist,
            @NotNull LoadBalancer.Settings loadBalancer
    ) {}

    public static class Tinder extends Particle.Tinder<ScalarFamily> {
        private final ScalarFamily.Settings settings;

        public Tinder(@NotNull Settings settings) {
            this.settings = settings;
        }

        @Override
        public @NotNull ScalarFamily ignite() throws Exception {
            Flux<Whitelist> whitelist = null;
            if (settings.whitelist() != null)
                whitelist = (new Whitelist.Tinder(settings.whitelist())).flux();

            Flux<LoadBalancer> loadBalancer = (switch (settings.loadBalancer().algorithm()) {
                case "ROUND_ROBIN" -> new RoundRobin.Tinder(settings.loadBalancer());
                case "LEAST_CONNECTION" -> new LeastConnection.Tinder(settings.loadBalancer());
                case "MOST_CONNECTION" -> new MostConnection.Tinder(settings.loadBalancer());
                default -> throw new RuntimeException("The id used for "+settings.id()+"'s load balancer is invalid!");
            }).flux();

            return new ScalarFamily(
                    this.settings.id(),
                    this.settings.displayName(),
                    this.settings.parent(),
                    whitelist,
                    loadBalancer
            );
        }
    }
}
