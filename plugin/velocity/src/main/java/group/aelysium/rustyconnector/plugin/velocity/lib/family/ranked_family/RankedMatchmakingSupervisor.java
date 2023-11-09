package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.PlayerRankLadder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import net.kyori.adventure.text.Component;

import java.util.List;

public class RankedMatchmakingSupervisor extends ClockService {
    private static int MAX_RANK = 50;
    private static int MIN_RANK = 0;
    protected RankedFamily family;
    protected RankedMatchmakerSettings settings;

    public RankedMatchmakingSupervisor(RankedMatchmakerSettings settings, RankedFamily family) {
        super(2);
        this.settings = settings;
        this.family = family;
    }

    /**
     * Take a group of players, validate their ranks, and create a game.
     * @param family The family to create the game in.
     * @param players The player to create a game with.
     * @param variance The variance that's allowed for the players to be a part of this game.
     * @throws IndexOutOfBoundsException When a player's rank is outside the variance allowed.
     */
    protected void handlePartition(RankedFamily family, List<RankablePlayer> players, double variance) {
        int middle = (int) Math.round(players.size() * 0.5);
        double pivot = players.get(middle).scorecard().rating().getConservativeRating();
        double bottom = players.get(0).scorecard().rating().getConservativeRating();
        double top = players.get(players.size() - 1).scorecard().rating().getConservativeRating();

        if(bottom < pivot - (variance * MAX_RANK)) throw new IndexOutOfBoundsException();
        if(top > pivot + (variance * MAX_RANK)) throw new IndexOutOfBoundsException();

        family.gameManager().start(players);
    }

    /**
     * Make as many partitions as possible and handle them.
     * @param query The query to start with.
     */
    protected void handleMultiplePartitions(RankedFamily family, PlayerRankLadder.PartitionQuery query) {
        try {
            List<List<RankablePlayer>> partitions = family.playerQueue().partition(query);

            for (List<RankablePlayer> partition : partitions) {
                if (partition.size() < query.min()) continue;

                try {
                    this.handlePartition(family, partition, settings.variance());
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {}
    }

    public void startSupervising() {
        this.scheduleDelayed(() -> {
            try {
                int max = family.gameManager().maxAllowedPlayers();
                int min = family.gameManager().minAllowedPlayers();
                double variance = settings.variance();

                if(family.playerQueue().size() < min) return;


                family.playerQueue().sort();
                List<RankablePlayer> players = family.playerQueue().players();

                if (players.size() <= max) {
                    this.handlePartition(family, family.playerQueue().players(), variance);
                    return;
                }


                PlayerRankLadder.PartitionQuery query = new PlayerRankLadder.PartitionQuery(settings.variance(), min, max);
                this.handleMultiplePartitions(family, query);
            } catch (Exception e) {
                Tinder.get().logger().send(Component.text("There was a fatal error while matchmaking the family: "+family.name()));
                e.printStackTrace();
            }

            this.startSupervising();
        }, settings.interval());
    }
}
