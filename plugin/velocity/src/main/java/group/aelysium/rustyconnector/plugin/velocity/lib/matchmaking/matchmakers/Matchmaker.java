package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Team;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;

public abstract class Matchmaker<TPlayerRank extends IPlayerRank<?>> implements Service {
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
        settings.teams.forEach(team -> {
            min[0] = min[0] + team.min();
            max[0] = max[0] + team.max();
        });

        this.minPlayersPerGame = min[0];
        this.maxPlayersPerGame = max[0];
    }

    /**
     * Using the players contained in the matchmaker, make a game.
     */
    public abstract Session make();
    public boolean minimumPlayersExist() {
        return this.waitingPlayers.size() > minPlayersPerGame;
    }
    public abstract void completeSort();

    /**
     * Inserts a player into the matchmaker.
     * <p>
     * Specifically, this method performs a {@link SingleSort#sort(List, int)} and injects the player into
     * an approximation of the best place for them to reside.
     * Thus reducing how frequently you'll need to perform a full sort on the metchmaker.
     * @param player The player to add.
     * @throws RuntimeException If there was an issue while adding the player to this matchmaker.
     */
    public void add(Player player) {
        try {
            RankedPlayer<TPlayerRank> rankedPlayer = this.settings.game.rankedPlayer(this.settings.storage(), player.uuid());

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
    public int size() {
        return this.waitingPlayers.size();
    }
    public List<RankedPlayer<TPlayerRank>> dump() {
        return this.waitingPlayers;
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

    public void kill() {
        this.supervisor.kill();
        this.waitingSessions.forEach(Session::end);
        this.waitingSessions.clear();

        this.runningSessions.forEach(Session::end);
        this.runningSessions.clear();
    }

    public record Settings (
            MySQLStorage storage,
            RankedGame game,
            List<Team.Settings> teams,
            double variance,
            LiquidTimestamp interval
    ) {}
}