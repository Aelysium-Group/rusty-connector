package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Team;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.floor;

public abstract class Matchmaker<TPlayerRank extends IPlayerRank<?>> {
    protected final ClockService supervisor = new ClockService(5);
    protected final Settings settings;
    protected final int minPlayersPerGame;
    protected final int maxPlayersPerGame;
    protected Vector<Session> sessions = new Vector<>();
    protected Vector<RankedPlayer<TPlayerRank>> items = new Vector<>();

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
        return this.items.size() > minPlayersPerGame;
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
            RankedPlayer<TPlayerRank> rankedPlayer = (RankedPlayer<TPlayerRank>) this.settings.game.rankedPlayer(this.settings.storage(), player.uuid());

            if (this.items.contains(rankedPlayer)) return;

            this.items.add(rankedPlayer);
            int index = this.items.size() - 1;

            SingleSort.sort(this.items, index);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void remove(RankedPlayer<TPlayerRank> item) {
        this.items.remove(item);
    }
    public int size() {
        return this.items.size();
    }
    public List<RankedPlayer<TPlayerRank>> dump() {
        return this.items;
    }
    public boolean contains(RankedPlayer<TPlayerRank> item) {
        return this.items.contains(item);
    }

    public void start(LoadBalancer loadBalancer) {
        // Sort players periodically
        this.supervisor.scheduleRecurring(this::completeSort, LiquidTimestamp.from(30, TimeUnit.SECONDS));

        // Build sessions periodically
        this.supervisor.scheduleRecurring(() -> {
            int playerCount = this.items.size();
            double approximateNumberOfGamesToRun = floor((double) ((playerCount / maxPlayersPerGame) + (playerCount / minPlayersPerGame)) / 2);

            for (int i = 0; i < approximateNumberOfGamesToRun; i++) {
                try {
                    Session session = this.make();
                    if(session == null) continue;

                    this.sessions.add(session);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, LiquidTimestamp.from(20, TimeUnit.SECONDS));

        // Connect sessions to a server periodically
        this.supervisor.scheduleRecurring(() -> {
            if(loadBalancer.size() == 0) return;

            for (Session session : this.sessions) {

            }
        }, LiquidTimestamp.from(10, TimeUnit.SECONDS));
    }

    public record Settings (
            MySQLStorage storage,
            RankedGame game,
            List<Team.Settings> teams,
            double variance,
            LiquidTimestamp interval
    ) {}
}