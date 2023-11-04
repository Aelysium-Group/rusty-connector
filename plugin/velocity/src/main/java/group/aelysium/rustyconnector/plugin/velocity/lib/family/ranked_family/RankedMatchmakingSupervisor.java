package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.api.core.serviceable.ClockService;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.util.ArrayList;
import java.util.List;

public class RankedMatchmakingSupervisor extends ClockService {
    private static int MAX_RANK = 50;
    private static int MIN_RANK = 0;

    protected List<RankedFamily> families = new ArrayList<>();
    protected RankedMatchmakerSettings settings;

    public RankedMatchmakingSupervisor(RankedMatchmakerSettings settings, List<RankedFamily> families) {
        super(4);
        this.settings = settings;
        this.families = families;
    }

    /**
     * Take a group of players, validate their ranks, and create a game.
     * @param family The family to create the game in.
     * @param players The player to create a game with.
     * @param variance The variance that's allowed for the players to be a part of this game.
     * @throws IndexOutOfBoundsException When a player's rank is outside the variance allowed.
     */
    public void handlePartition(RankedFamily family, List<RankablePlayer> players, double variance) {
        int middle = (int) Math.round(players.size() * 0.5);
        double pivot = players.get(middle).scorecard().rating().getConservativeRating();
        double bottom = players.get(0).scorecard().rating().getConservativeRating();
        double top = players.get(players.size() - 1).scorecard().rating().getConservativeRating();

        if(bottom < pivot - (variance * MAX_RANK)) throw new IndexOutOfBoundsException();
        if(top > pivot + (variance * MAX_RANK)) throw new IndexOutOfBoundsException();

        family.gameManager().start(players);
    }

    public void startSupervising() {
        for (RankedFamily family : this.families) {
            int max = family.gameManager().maxAllowedPlayers();
            int min = family.gameManager().minAllowedPlayers();
            double variance = settings.variance();
            List<RankablePlayer> players = family.playerQueue().players();

            if(players.size() < min) return; // Not large enough to even start one game

            family.playerQueue().sort();

            if(players.size() > max) {
                List<List<RankablePlayer>> partitions = family.playerQueue().partition(variance, max);

                for (List<RankablePlayer> partition : partitions) {
                    if(partition.size() < min) continue;

                    try {
                        this.handlePartition(family, partition, variance);
                    } catch (Exception ignore) {}
                }
            } // issue a partition

            // Enough to start a game maybe
            this.handlePartition(family, players, variance);
        }
    }
}
