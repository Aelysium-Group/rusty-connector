package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.matchmakers.IMatchmaker;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IScoreCard.IRankSchema.*;
import static java.lang.Math.floor;

public abstract class Matchmaker<TPlayerRank extends IPlayerRank<?>> implements IMatchmaker<Player, RankedPlayer<TPlayerRank>> {
    protected final ClockService supervisor = new ClockService(5);
    protected final Settings settings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected Vector<Session> waitingSessions = new Vector<>();
    protected Vector<Session> runningSessions = new Vector<>();
    protected Vector<RankedPlayer<TPlayerRank>> waitingPlayers = new Vector<>();

    public Matchmaker(Settings settings) {
        this.settings = settings;

        final int[] min = {0};
        final int[] max = {0};
        settings.teams().forEach(team -> {
            min[0] = min[0] + team.min();
            max[0] = max[0] + team.max();
        });

        this.minPlayersPerGame = min[0];
        this.maxPlayersPerGame = max[0];
    }

    public abstract Session make();
    public boolean minimumPlayersExist() {
        return this.waitingPlayers.size() > minPlayersPerGame;
    }
    public abstract void completeSort();

    public void add(Player player) {
        try {
            RankedPlayer<TPlayerRank> rankedPlayer = this.settings.game().rankedPlayer(this.settings.storage(), player.uuid());

            if (this.waitingPlayers.contains(rankedPlayer)) return;

            this.waitingPlayers.add(rankedPlayer);
            int index = this.waitingPlayers.size() - 1;

            SingleSort.sort(this.waitingPlayers, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void remove(Player player) {
        try {
            this.waitingPlayers.removeIf(player1 -> player1.uuid().equals(player1.uuid()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public List<RankedPlayer<TPlayerRank>> waitingPlayers() {
        return this.waitingPlayers.stream().toList();
    }
    public boolean contains(RankedPlayer<TPlayerRank> item) {
        return this.waitingPlayers.contains(item);
    }

    public void start(LoadBalancer loadBalancer) {
        // Sort players periodically
        this.supervisor.scheduleRecurring(this::completeSort, LiquidTimestamp.from(30, TimeUnit.SECONDS));

        // Build sessions periodically
        this.supervisor.scheduleRecurring(() -> {
            int playerCount = this.waitingPlayers.size();
            double approximateNumberOfGamesToRun = floor((double) ((playerCount / maxPlayersPerGame) + (playerCount / minPlayersPerGame)) / 2);

            for (int i = 0; i < approximateNumberOfGamesToRun; i++) {
                try {
                    Session session = this.make();
                    if(session == null) continue;

                    this.waitingSessions.add(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, LiquidTimestamp.from(20, TimeUnit.SECONDS));

        // Connect sessions to a server periodically
        this.supervisor.scheduleRecurring(() -> {
            if(loadBalancer.size() == 0) return;

            List<Session> sessionsForLooping = this.waitingSessions.stream().toList();
            for (Session session : sessionsForLooping) {
                try {
                    MCLoader server = loadBalancer.current();
                    session.connect(server);

                    loadBalancer.lock(server);

                    this.waitingSessions.remove(session);
                    this.runningSessions.add(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, LiquidTimestamp.from(10, TimeUnit.SECONDS));
    }

    public static Matchmaker<? extends IPlayerRank<?>> from(Settings settings) {
        if (settings.algorithm().equals(WIN_LOSS)) return new WinLoss(settings);
        if (settings.algorithm().equals(WIN_RATE)) return new WinRate(settings);

        return new Randomized(settings);
    }

    public void kill() {
        this.supervisor.kill();
        this.waitingSessions.forEach(Session::end);
        this.waitingSessions.clear();

        this.runningSessions.forEach(Session::end);
        this.runningSessions.clear();
    }
}