package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer.RankKey;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static group.aelysium.rustyconnector.toolkit.velocity.util.DependencyInjector.inject;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class Matchmaker implements IMatchmaker<IPlayerRank> {
    protected final ClockService supervisor = new ClockService(5);
    protected final ClockService queueIndicator = new ClockService(1);
    protected final StorageService storage;
    protected final String gameId;
    protected final Settings settings;
    protected final ISession.Settings sessionSettings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected BossBar waitingForPlayers = BossBar.bossBar(
            Component.text("Waiting for players...").color(GRAY),
            (float) 0.0,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS
    );
    protected BossBar waitingForServers = BossBar.bossBar(
            Component.text("Waiting for servers...").color(GRAY),
            (float) 0.0,
            BossBar.Color.WHITE,
            BossBar.Overlay.PROGRESS
    );
    protected Map<UUID, ISession> sessions = new ConcurrentHashMap<>();
    protected Vector<UUID> queuedSessions = new Vector<>();
    protected Vector<UUID> activeSessions = new Vector<>();
    protected Map<UUID, ISession> players = new ConcurrentHashMap<>();

    public Matchmaker(Settings settings, StorageService storage, String gameId) {
        this.storage = storage;
        this.gameId = gameId;
        this.settings = settings;
        this.sessionSettings = new ISession.Settings(settings.session().freezeActiveSessions(), settings.session().closingThreshold(), settings.session().max(), settings.ranking().variance(), this.gameId);

        this.minPlayersPerGame = settings.session().min();
        this.maxPlayersPerGame = settings.session().max();
    }

    /**
     * Gets the matched player in this matchmaker.
     * If the player isn't in this matchmaker, this will return an empty optional.
     */
    protected Optional<IMatchPlayer<IPlayerRank>> matchPlayer(IPlayer player) {
        ISession session = this.players.get(player.uuid());
        if(session == null) return Optional.empty();
        IMatchPlayer<IPlayerRank> matchPlayer = session.players().get(player.uuid());
        if(matchPlayer == null) {
            this.players.remove(player.uuid()); // matchPlayer shouldn't be null here. If it is, the player isn't in the matchmaker.
            return Optional.empty();
        }
        return Optional.of(matchPlayer);
    }

    /**
     * Attempts to connect the player to the session.
     */
    protected ConnectionResult connectSession(ISession session, IMatchPlayer<IPlayerRank> matchPlayer) throws ExecutionException, InterruptedException, TimeoutException {
        ConnectionResult result = session.join(matchPlayer).result().get(5, TimeUnit.SECONDS);

        if(result.connected()) {
            this.players.put(matchPlayer.player().uuid(), session);
            if(!this.sessions.containsKey(session.uuid())) this.sessions.put(session.uuid(), session);
        }

        return result;
    }

    /**
     * Prepares a session for the passed player.
     * This method does not save the session, nor does it connect the player to it.
     * To connect a player to the session use {@link Matchmaker#connectSession(ISession, IMatchPlayer)}
     */
    protected ISession prepareSession(IMatchPlayer<IPlayerRank> matchPlayer) {
        double rank = matchPlayer.rank().rank();
        ISession chosenSession = null;
        for (ISession session : this.sessions.values()) {
            if(!session.range().validate(rank)) continue;
            if(session.full()) continue;
            chosenSession = session;
            break;
        }

        if(chosenSession == null) chosenSession = new Session(matchPlayer, sessionSettings);

        return chosenSession;
    }

    /**
     * Resolves a player rank for the player.
     */
    protected IPlayerRank resolvePlayerRank(IPlayer player) {
        RankKey key = RankKey.from(player.uuid(), this.gameId);

        return this.storage.database().fetchRank(key).orElseGet(()->{
            IPlayerRank newRank = this.newPlayerRank();
            System.out.println(newRank);

            if(newRank instanceof RandomizedPlayerRank) return newRank;
            // No data is used by RandomizedPlayerRank. It would literally just waste space to store it.

            storage.database().saveRank(key, newRank);

            return newRank;
        });
    }

    public Settings settings() {
        return this.settings;
    }
    public String gameId() {
        return this.gameId;
    }

    public void queue(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result) {
        IPlayer player = request.player();
        try {
            RankKey key = RankKey.from(player.uuid(), this.gameId);
            IPlayerRank rank = this.resolvePlayerRank(player);

            IMatchPlayer<IPlayerRank> matchPlayer = new MatchPlayer(player, rank, key.gameId());

            if(this.players.containsKey(matchPlayer.player().uuid())) throw new RuntimeException("Player is already queued!");

            if (this.settings.queue().joining().reconnect())
                for (ISession session : this.sessions.values()) {
                    if(!session.contains(matchPlayer)) continue;

                    ConnectionResult reconnectResult = session.join(matchPlayer).result().get(5, TimeUnit.SECONDS);
                    if(!reconnectResult.connected()) break;

                    result.complete(ConnectionResult.success(Component.text("You've successfully reconnected to your session!"), reconnectResult.server().orElse(null)));
                }

            ISession session = this.prepareSession(matchPlayer);
            ConnectionResult sessionConnectResult = this.connectSession(session, matchPlayer);
            if(!sessionConnectResult.connected()) throw new RuntimeException("Unable to connect to a session.");
        } catch (Exception e) {
            result.complete(ConnectionResult.failed(Component.text("There was an issue queuing into matchmaking!")));
            throw new RuntimeException(e);
        }
        result.complete(ConnectionResult.success(Component.text("Successfully queued into the matchmaker!"), null));
    }

    public boolean remove(IPlayer player) {
        try {
            hideBossBars(player.resolve().orElseThrow());
        } catch (Exception ignore) {}

        ISession session = this.players.get(player.uuid());
        if(session == null) return false;
        Optional<IMatchPlayer<IPlayerRank>> matchPlayer = this.matchPlayer(player); // Will remove the player from this.players if they aren't valid
        if(matchPlayer.isEmpty()) return false;
        return session.leave(matchPlayer.get());
    }

    public boolean contains(IPlayer player) {
        return this.players.containsKey(player.uuid());
    }

    public Optional<ISession> fetchPlayerSession(UUID playerUUID) {
        ISession session = this.players.get(playerUUID);
        if(session == null) return Optional.empty();
        return Optional.of(session);
    }

    public Optional<ISession> fetch(UUID sessionUUID) {
        ISession session = this.sessions.get(sessionUUID);
        if(session == null) return Optional.empty();
        return Optional.of(session);
    }

    public void hideBossBars(Player player) {
        player.hideBossBar(this.waitingForPlayers);
        player.hideBossBar(this.waitingForServers);
    }

    public void start(ILoadBalancer<IMCLoader> loadBalancer) {
        // Connect sessions to a server periodically
        this.supervisor.scheduleRecurring(() -> {
            if(loadBalancer.size(false) == 0) return;

            for (UUID uuid : this.queuedSessions.stream().toList()) {
                if(loadBalancer.size(false) == 0) return;

                Optional<ISession> optionalSession = this.fetch(uuid);
                if(optionalSession.isEmpty()) { // If empty, the session no-longer exists and shouldn't be here.
                    this.queuedSessions.remove(uuid);
                    continue;
                }
                ISession session = optionalSession.get();

                try {
                    RankedMCLoader server = (RankedMCLoader) loadBalancer.current().orElseThrow(
                            () -> new RuntimeException("There are no servers to connect to!")
                    );

                    session.start(server);

                    this.queuedSessions.remove(session.uuid());
                    this.activeSessions.add(session.uuid());

                    session.players().values().forEach(matchPlayer -> {
                        try {
                            Player velocityPlayer = matchPlayer.player().resolve().orElseThrow();
                            hideBossBars(velocityPlayer);
                        } catch (Exception ignore) {}
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }, LiquidTimestamp.from(10, TimeUnit.SECONDS));

        if(!this.settings.queue().joining().showInfo()) return;
        this.queueIndicator.scheduleRecurring(() -> {
            // So that we don't lock the Vector while sending messages
            for (UUID uuid : this.queuedSessions.stream().toList()) {
                Optional<ISession> optionalSession = this.fetch(uuid);
                if(optionalSession.isEmpty()) continue;
                ISession session = optionalSession.get();

                for (IMatchPlayer<IPlayerRank> matchPlayer : session.players().values())
                    try {
                        Player velocityPlayer = matchPlayer.player().resolve().orElseThrow();

                        hideBossBars(velocityPlayer);
                        velocityPlayer.sendActionBar(Component.text("----< MATCHMAKING >----", NamedTextColor.YELLOW));

                        if(session.size() < session.settings().min()) {
                            Bossbar.WAITING_FOR_SERVERS(this.waitingForServers, loadBalancer.size(true), loadBalancer.size(false));
                            velocityPlayer.showBossBar(this.waitingForServers);
                            continue;
                        }

                        Bossbar.WAITING_FOR_PLAYERS(this.waitingForPlayers, session.size(), maxPlayersPerGame);
                        velocityPlayer.showBossBar(this.waitingForPlayers);
                    } catch (Exception ignore) {}
            }
        }, LiquidTimestamp.from(4, TimeUnit.SECONDS));
    }

    public int playerCount() {
        return this.players.size();
    }

    public int queuedPlayerCount() {
        AtomicInteger count = new AtomicInteger();

        for (UUID uuid : this.queuedSessions.stream().toList()) {
            try {
                ISession session = this.fetch(uuid).orElseThrow();
                count.addAndGet(session.size());
            } catch (Exception e) { // This exception should never throw. If it does, the uuid needs to be removed from the waiting sessions vector.
                this.queuedSessions.remove(uuid);
            }
        }

        return count.get();
    }

    public int activePlayerCount() {
        AtomicInteger count = new AtomicInteger();

        for (UUID uuid : this.activeSessions.stream().toList()) {
            try {
                ISession session = this.fetch(uuid).orElseThrow();
                count.addAndGet(session.size());
            } catch (Exception e) { // This exception should never throw. If it does, the uuid needs to be removed from the waiting sessions vector.
                this.queuedSessions.remove(uuid);
            }
        }

        return count.get();
    }

    public int sessionCount() {
        return this.sessions.size();
    }

    public int queuedSessionCount() {
        return this.queuedSessions.size();
    }

    public int activeSessionCount() {
        return this.activeSessions.size();
    }

    protected IPlayerRank newPlayerRank() {
        try {
            return settings.ranking().schema().getConstructor().newInstance();
        } catch(Exception e) {
            e.printStackTrace();
            return new RandomizedPlayerRank();
        }
    }

    public void remove(ISession session) {
        this.activeSessions.remove(session.uuid());
        this.queuedSessions.remove(session.uuid());
        session.players().keySet().forEach(k->this.players.remove(k));
        this.sessions.remove(session.uuid());
    }

    public void kill() {
        this.supervisor.kill();
        this.queueIndicator.kill();

        this.queuedSessions.clear();
        this.activeSessions.clear();

        this.sessions.values().forEach(session -> session.end(List.of(), List.of()));
        this.sessions.clear();

        this.players.clear();
    }

    protected interface Bossbar {
        static void WAITING_FOR_PLAYERS(BossBar bossbar, int players, int max) {
            float percentage = (float) players / max;

            BossBar.Color color = BossBar.Color.WHITE;
            if (percentage > 0.5) color = BossBar.Color.YELLOW;
            if (percentage >= 1) color = BossBar.Color.GREEN;

            bossbar.color(color);
            bossbar.progress(percentage);
        }

        static void WAITING_FOR_SERVERS(BossBar bossbar, int closedServers, int openServers) {
            int totalServers = closedServers + openServers;
            float percentage = (float) openServers / totalServers;

            bossbar.color(BossBar.Color.BLUE);
            bossbar.progress(percentage);
        }
    }
}