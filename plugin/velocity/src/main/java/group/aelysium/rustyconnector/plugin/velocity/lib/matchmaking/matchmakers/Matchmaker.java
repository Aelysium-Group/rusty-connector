package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.matchmakers.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.connection.ConnectionRequest;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard.IRankSchema.*;
import static java.lang.Math.floor;

public abstract class Matchmaker implements IMatchmaker {
    protected final ClockService supervisor = new ClockService(5);
    protected final Settings settings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected Map<UUID, ISession.IWaiting> waitingSessions = new ConcurrentHashMap<>();
    protected Map<UUID, ISession> runningSessions = new ConcurrentHashMap<>();
    protected Vector<IRankedPlayer> waitingPlayers = new Vector<>();
    protected Map<UUID, CompletableFuture<ConnectionRequest.Result>> pendingConnectionRequests = new ConcurrentHashMap<>();

    public Matchmaker(Settings settings) {
        this.settings = settings;

        this.minPlayersPerGame = settings.min();
        this.maxPlayersPerGame = settings.max();
    }

    public abstract group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session.Waiting make();
    public boolean minimumPlayersExist() {
        return this.waitingPlayers.size() > minPlayersPerGame;
    }
    public abstract void completeSort();

    public void add(ConnectionRequest request, CompletableFuture<ConnectionRequest.Result> result) {
        try {
            IRankedPlayer rankedPlayer = this.settings.game().rankedPlayer(this.settings.storage(), request.player().uuid());

            if (this.waitingPlayers.contains(rankedPlayer)) return;

            this.waitingPlayers.add(rankedPlayer);
            int index = this.waitingPlayers.size() - 1;

            SingleSort.sort(this.waitingPlayers, index);
            this.pendingConnectionRequests.put(rankedPlayer.uuid(), result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void remove(IPlayer player) {
        try {
            this.waitingPlayers.removeIf(player1 -> player1.uuid().equals(player.uuid()));

            CompletableFuture<ConnectionRequest.Result> result = this.pendingConnectionRequests.get(player.uuid());
            if(result == null) return;
            this.pendingConnectionRequests.remove(player.uuid());
            result.complete(ConnectionRequest.Result.failed(Component.text("Player left matchmaking.")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<IRankedPlayer> waitingPlayers() {
        return this.waitingPlayers.stream().toList();
    }
    public boolean contains(IRankedPlayer item) {
        return this.waitingPlayers.contains(item);
    }

    public void start(ILoadBalancer<IMCLoader> loadBalancer) {
        // Sort players periodically
        this.supervisor.scheduleRecurring(this::completeSort, LiquidTimestamp.from(30, TimeUnit.SECONDS));

        // Build sessions periodically
        this.supervisor.scheduleRecurring(() -> {
            int playerCount = this.waitingPlayers.size();
            if(playerCount < minPlayersPerGame) return;

            double approximateNumberOfGamesToRun = floor((double) ((playerCount / maxPlayersPerGame) + (playerCount / minPlayersPerGame)) / 2);

            for (int i = 0; i < approximateNumberOfGamesToRun; i++) {
                try {
                    Session.Waiting session = this.make();
                    if(session == null) continue;

                    this.waitingSessions.put(session.uuid(), session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, LiquidTimestamp.from(20, TimeUnit.SECONDS));

        // Connect sessions to a server periodically
        this.supervisor.scheduleRecurring(() -> {
            if(loadBalancer.size(false) == 0) return;

            for (ISession.IWaiting waitingSession : this.waitingSessions.values().stream().toList()) {
                if(loadBalancer.size(false) == 0) break;
                try {
                    RankedMCLoader server = (RankedMCLoader) loadBalancer.current().orElse(null);
                    if(server == null) throw new RuntimeException("There are no servers to connect to!");
                    ISession session = waitingSession.start(server);

                    this.waitingSessions.remove(waitingSession.uuid());
                    this.runningSessions.put(session.uuid(), session);

                    session.players().forEach(player -> {
                        CompletableFuture<ConnectionRequest.Result> result = this.pendingConnectionRequests.get(player.uuid());
                        if(result == null) return;
                        this.pendingConnectionRequests.remove(player.uuid());
                        result.complete(ConnectionRequest.Result.success(Component.text("You successfully joined a game!"), server));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, LiquidTimestamp.from(10, TimeUnit.SECONDS));
    }

    public static Matchmaker from(Settings settings) {
        if (settings.algorithm().equals(WIN_LOSS)) return new WinLoss(settings);
        if (settings.algorithm().equals(WIN_RATE)) return new WinRate(settings);

        return new Randomized(settings);
    }

    public void remove(ISession session) {
        session.players().forEach(player -> {
            CompletableFuture<ConnectionRequest.Result> result = this.pendingConnectionRequests.get(player.uuid());
            if(result == null) return;
            this.pendingConnectionRequests.remove(player.uuid());
            result.complete(ConnectionRequest.Result.failed(Component.text("You left the server before you could join a game!")));
        });
        this.waitingSessions.remove(session.uuid());
        this.runningSessions.remove(session.uuid());
    }

    public void kill() {
        this.supervisor.kill();
        this.waitingSessions.clear();

        this.runningSessions.values().forEach(ISession::end);
        this.runningSessions.clear();

        this.pendingConnectionRequests.values().forEach(item -> item.cancel(true));
        this.pendingConnectionRequests.clear();
    }
}