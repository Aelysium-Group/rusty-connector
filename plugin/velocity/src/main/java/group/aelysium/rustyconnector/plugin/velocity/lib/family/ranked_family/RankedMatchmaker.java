package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedSoloGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedTeamGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.PlayerRankLadder;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class RankedMatchmaker extends ClockService {
    private static int MAX_RANK = 50;
    private static int MIN_RANK = 0;
    protected Settings settings;
    protected RankedFamily owner;
    protected PlayerRankLadder waitingPlayers;
    protected Vector<RankedGame> waitingGames = new Vector<>();

    public RankedMatchmaker(Settings settings, RankedFamily owner, PlayerRankLadder waitingPlayers) {
        super(4);
        this.settings = settings;
        this.owner = owner;
        this.waitingPlayers = waitingPlayers;
    }

    /**
     * Queues a game to be connected to an MCLoader once one is available.
     * @throws IllegalStateException If the game is already over or already has a server.
     */
    public void queueGame(RankedGame game) {
        if(game.server() != null) throw new IllegalStateException("This game already has a server!");
        if(game.ended()) throw new IllegalStateException("This game has already ended!");
    }

    public void startSupervising() {
        this.matchmakingProcess();
        this.serverAssignmentProcess();
    }

    /**
     * Take a group of players, validate their ranks, and create a game.
     * The game will then be queued to be loaded into an MCLoader.
     * @param players The player to create a game with.
     * @param variance The variance that's allowed for the players to be a part of this game.
     * @throws IndexOutOfBoundsException When a player's rank is outside the variance allowed.
     */
    protected void createGame(List<RankedPlayer> players, double variance) {
        int middle = (int) Math.round(players.size() * 0.5);
        double pivot = players.get(middle).scorecard().rating().getConservativeRating();
        double bottom = players.get(0).scorecard().rating().getConservativeRating();
        double top = players.get(players.size() - 1).scorecard().rating().getConservativeRating();

        if(bottom < pivot - (variance * MAX_RANK)) throw new IndexOutOfBoundsException();
        if(top > pivot + (variance * MAX_RANK)) throw new IndexOutOfBoundsException();

        {
            RankedGame game = null;
            if (settings.soloSettings() != null) {
                game = RankedSoloGame.startNew(settings.soloSettings(), players);
                this.waitingPlayers.remove(game.players());
            }
            if (settings.teamSettings() != null) {
                game = RankedTeamGame.startNew(settings.teamSettings(), players);
                this.waitingPlayers.remove(game.players());
            }

            if (game == null) throw new NullPointerException("Unable to create a new game!");

            this.waitingGames.add(game);
        }

    }

    /**
     * Make as many partitions as possible and handle them.
     * @param query The query to start with.
     */
    protected void handleMultiplePartitions(PlayerRankLadder.PartitionQuery query) {
        try {
            List<List<RankedPlayer>> partitions = owner.gameManager().playerQueue().partition(query);

            for (List<RankedPlayer> partition : partitions) {
                if (partition.size() < query.min()) continue;

                try {
                    this.createGame(partition, settings.variance());
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {}
    }

    protected void matchmakingProcess() {
        this.scheduleDelayed(() -> {
            try {
                int max = RankedGameManager.maxAllowedPlayers(this.settings);
                int min = RankedGameManager.minAllowedPlayers(this.settings);
                double variance = settings.variance();

                if(this.waitingPlayers.size() < min) return;


                this.waitingPlayers.sort();
                List<RankedPlayer> players = this.waitingPlayers.players();

                if (players.size() <= max) {
                    this.createGame(this.waitingPlayers.players(), variance);
                    return;
                }


                PlayerRankLadder.PartitionQuery query = new PlayerRankLadder.PartitionQuery(settings.variance(), min, max);
                this.handleMultiplePartitions(query);
            } catch (Exception e) {
                Tinder.get().logger().send(Component.text("There was a fatal error while matchmaking the family: "+owner.id()));
                e.printStackTrace();
            }

            this.matchmakingProcess();
        }, settings.interval());
    }

    protected void serverAssignmentProcess() {
        this.scheduleDelayed(() -> {
            try {
                LoadBalancer loadBalancer = this.owner.loadBalancer();
                if(loadBalancer.size() == 0) return;
                if(this.waitingGames.size() == 0) return;

                List<RankedGame> successfullyStartedGames = new ArrayList<>();
                for (RankedGame game : this.waitingGames) {
                    MCLoader server = loadBalancer.current();
                    game.connectServer(server);

                    owner.lockServer(server);
                    successfullyStartedGames.add(game);
                }

                this.waitingGames.removeAll(successfullyStartedGames);
            } catch (Exception e) {
                Tinder.get().logger().send(Component.text("There was a fatal error while matchmaking the family: "+owner.id()));
                e.printStackTrace();
            }

            this.serverAssignmentProcess();
        }, settings.interval());
    }

    public record Settings (
            String name,
            RankedGame.RankerType type,
            RankedSoloGame.Settings soloSettings,
            RankedTeamGame.Settings teamSettings,
            RankedGame.ScoringType scoringType,
            double variance,
            LiquidTimestamp interval
    ) {}
}
