package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.DefaultRankResolver;
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
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class Matchmaker implements IMatchmaker {
    protected final ClockService supervisor = new ClockService(5);
    protected final ClockService queueIndicator = new ClockService(1);
    protected final StorageService storage;
    protected final String gameId;
    protected final Settings settings;
    protected final ISession.Settings sessionSettings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected final AtomicInteger failedBuilds = new AtomicInteger();
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
    protected Map<UUID, ISession> activeSessions = new ConcurrentHashMap<>();
    protected Map<UUID, ISession> queuedSessions = new ConcurrentHashMap<>();
    protected Map<UUID, ISession> sessionPlayers = new ConcurrentHashMap<>();
    protected List<IMatchPlayer<IPlayerRank>> queuedPlayers = Collections.synchronizedList(new ArrayList<>());


    public Matchmaker(Settings settings, StorageService storage, String gameId) {
        this.storage = storage;
        this.gameId = gameId;
        this.settings = settings;
        this.sessionSettings = new ISession.Settings(
                settings.session().freezeActiveSessions(),
                settings.session().closingThreshold(),
                settings.session().max(),
                settings.ranking().variance(),
                this.gameId,
                settings.session().quittersLose(),
                settings.session().stayersWin()
        );

        this.minPlayersPerGame = settings.session().min();
        this.maxPlayersPerGame = settings.session().max();
    }

    public Settings settings() {
        return this.settings;
    }
    public String gameId() {
        return this.gameId;
    }

    public Optional<IMatchPlayer<IPlayerRank>> matchPlayer(IPlayer player) {
        Optional<IPlayerRank> rank = this.storage.database().ranks().get(player, this.gameId, DefaultRankResolver.New());
        return rank.map(r -> new MatchPlayer(player, r, this.gameId));
    }

    public synchronized void queue(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result) {
        IPlayer player = request.player();
        try {
            IMatchPlayer<IPlayerRank> matchPlayer = this.resolveMatchPlayer(player);

            if(this.sessionPlayers.containsKey(matchPlayer.player().uuid())) throw new RuntimeException("Player is already queued!");

            int insertIndex = this.queuedPlayers.size();
            this.queuedPlayers.add(insertIndex, matchPlayer);
            SingleSort.sort(this.queuedPlayers, insertIndex);
        } catch (Exception e) {
            result.complete(ConnectionResult.failed(Component.text("There was an issue queuing into matchmaking!")));
            throw new RuntimeException(e);
        }
        result.complete(ConnectionResult.success(Component.text("Successfully queued into the matchmaker!"), null));
    }

    public void leave(IPlayer player) {
        if(!this.sessionPlayers.containsKey(player.uuid())) return;

        try {
            hideBossBars(player.resolve().orElseThrow());
        } catch (Exception ignore) {}

        ((Session) this.sessionPlayers.get(player.uuid())).leave(player);

        this.sessionPlayers.remove(player.uuid());
    }

    public boolean contains(IPlayer player) {
        return this.sessionPlayers.containsKey(player.uuid());
    }

    public Optional<ISession> fetchPlayersSession(UUID playerUUID) {
        ISession session = this.sessionPlayers.get(playerUUID);
        if(session == null) return Optional.empty();
        return Optional.of(session);
    }

    public Optional<ISession> fetch(UUID sessionUUID) {
        ISession session = this.activeSessions.get(sessionUUID);
        if(session == null) session = this.queuedSessions.get(sessionUUID);
        if(session == null) return Optional.empty();
        return Optional.of(session);
    }

    public void hideBossBars(Player player) {
        player.hideBossBar(this.waitingForPlayers);
        player.hideBossBar(this.waitingForServers);
    }

    public void start(ILoadBalancer<IMCLoader> loadBalancer) {
        this.supervisor.scheduleRecurring(() -> {
            int i = 0;
            double varianceLookahead = (this.settings.ranking().variance() + (this.settings.ranking().varianceExpansionCoefficient() * this.failedBuilds.get())) * 2;
            List<IMatchPlayer<IPlayerRank>> removePlayers = new ArrayList<>();
            List<ISession> builtSessions = new ArrayList<>();
            while(i < this.queuedPlayers.size()) {
                // If a session fills up to max players, minPlayers won't be enough to jump the gap thus resulting in duplicate players in different sessions.
                int nextHop = this.minPlayersPerGame;
                try {
                    IMatchPlayer<IPlayerRank> current = this.queuedPlayers.get(i);
                    IMatchPlayer<IPlayerRank> thrown = this.queuedPlayers.get(i + this.minPlayersPerGame);

                    double varianceMax = (current.rank() + varianceLookahead);

                    if(varianceMax < thrown.rank()) {
                        i = i + this.minPlayersPerGame;
                        continue;
                    }

                    ISession session = new Session(this, this.sessionSettings);
                    try {
                        for (int j = i; j < i + maxPlayersPerGame; j++) {
                            IMatchPlayer<IPlayerRank> nextInsert = this.queuedPlayers.get(j);
                            if(varianceMax < nextInsert.rank()) throw new IndexOutOfBoundsException();
                            session.join(nextInsert);
                        }
                    } catch (IndexOutOfBoundsException ignore) {}
                    if(session.size() < session.settings().min()) throw new NoOutputException();

                    builtSessions.add(session);
                    removePlayers.addAll(session.players().values());
                    nextHop = session.size();
                } catch (IndexOutOfBoundsException | NoOutputException ignore) {}
                i = i + nextHop;
            }

            if(builtSessions.isEmpty() && this.queuedPlayers.size() > this.minPlayersPerGame) this.failedBuilds.incrementAndGet();
            if(!builtSessions.isEmpty()) this.failedBuilds.set(0);

            this.queuedPlayers.removeAll(removePlayers);
            builtSessions.forEach(s -> this.queuedSessions.put(s.uuid(), s));
        }, LiquidTimestamp.from(10, TimeUnit.SECONDS));

        this.supervisor.scheduleRecurring(() -> {
            if(loadBalancer.size(false) == 0) return;

            for (ISession session : this.queuedSessions.values()) {
                if(loadBalancer.size(false) == 0) return;

                try {
                    if (session.size() < session.settings().min()) {
                        session.implode("There are not enough players to start a game!");
                        continue;
                    }

                    RankedMCLoader server = (RankedMCLoader) loadBalancer.current().orElseThrow(
                            () -> new RuntimeException("There are no servers to connect to!")
                    );

                    session.start(server);

                    this.queuedSessions.remove(session.uuid());
                    this.activeSessions.put(session.uuid(), session);

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
            for(IMatchPlayer<IPlayerRank> matchPlayer : this.queuedPlayers.stream().toList()) {
                Player velocityPlayer = matchPlayer.player().resolve().orElseThrow();

                hideBossBars(velocityPlayer);
                velocityPlayer.sendActionBar(Component.text("----< MATCHMAKING >----", NamedTextColor.YELLOW));

                Bossbar.WAITING_FOR_PLAYERS(this.waitingForPlayers, 0, maxPlayersPerGame);
                velocityPlayer.showBossBar(this.waitingForPlayers);
            }
            for (ISession session : this.queuedSessions.values())
                for (IMatchPlayer<IPlayerRank> matchPlayer : session.players().values())
                    try {
                        Player velocityPlayer = matchPlayer.player().resolve().orElseThrow();

                        hideBossBars(velocityPlayer);
                        velocityPlayer.sendActionBar(Component.text("----< MATCHMAKING >----", NamedTextColor.YELLOW));

                        Bossbar.WAITING_FOR_SERVERS(this.waitingForServers, loadBalancer.size(true), loadBalancer.size(false));
                        velocityPlayer.showBossBar(this.waitingForServers);
                    } catch (Exception ignore) {}
        }, LiquidTimestamp.from(5, TimeUnit.SECONDS));
    }

    public int playerCount() {
        return this.sessionPlayers.size() + this.queuedPlayers.size();
    }

    public int queuedPlayerCount() {
        AtomicInteger count = new AtomicInteger();

        for (ISession session : this.queuedSessions.values())
            count.addAndGet(session.size());

        count.addAndGet(this.queuedPlayers.size());

        return count.get();
    }

    public int activePlayerCount() {
        AtomicInteger count = new AtomicInteger();

        for (ISession session : this.activeSessions.values())
            count.addAndGet(session.size());

        return count.get();
    }

    public int sessionCount() {
        return this.activeSessions.size() + this.queuedSessions.size();
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
            if(!settings.ranking().schema().equals(RandomizedPlayerRank.class)) e.printStackTrace();
            return RandomizedPlayerRank.New();
        }
    }

    /**
     * Attempts to connect the player to the session.
     */
    protected ConnectionResult connectSession(ISession session, IMatchPlayer<IPlayerRank> matchPlayer) throws ExecutionException, InterruptedException, TimeoutException {
        if(!session.matchmaker().equals(this)) throw new RuntimeException("Attempted to connect to a session governed by anotehr matchmaker!");
        ConnectionResult result = session.join(matchPlayer).result().get(5, TimeUnit.SECONDS);

        if(result.connected())
            this.sessionPlayers.put(matchPlayer.player().uuid(), session);

        return result;
    }

    /**
     * Resolves a player rank for the player.
     */
    protected IMatchPlayer<IPlayerRank> resolveMatchPlayer(IPlayer player) {
        IPlayerRank rank = this.storage.database().ranks().get(player, this.gameId, DefaultRankResolver.New()).orElseGet(()->{
            IPlayerRank newRank = this.newPlayerRank();

            this.storage.database().ranks().set(new MatchPlayer(player, newRank, this.gameId));

            return newRank;
        });

        return new MatchPlayer(player, rank, this.gameId);
    }

    public void leave(ISession session) {
        session.players().keySet().forEach(k->this.sessionPlayers.remove(k));
        this.activeSessions.remove(session.uuid());
        this.queuedSessions.remove(session.uuid());
    }

    public void kill() {
        this.supervisor.kill();
        this.queueIndicator.kill();

        this.queuedSessions.clear();
        this.activeSessions.clear();

        this.queuedPlayers.clear();
        this.sessionPlayers.clear();
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