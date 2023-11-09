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

    protected List<RankedFamily> families;
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

    /**
     * Run a partition. If conditions are correct, expand the search and run another partition.
     * Method is recursive and will keep expanding and partitioning until {@link MatchmakerExpansionSettings#maxVariance()} is met.
     * @param query The query to start with.
     */
    protected void dynamicPartition(RankedFamily family, PlayerRankLadder.PartitionQuery query) {
        try {
            List<List<RankablePlayer>> partitions = family.playerQueue().partition(query);

            for (List<RankablePlayer> partition : partitions) {
                if (partition.size() < query.min()) continue;

                try {
                    this.handlePartition(family, partition, settings.variance());
                } catch (Exception ignore) {
                }
            }
        } catch (IndexOutOfBoundsException ignore) {
            this.dynamicPartition(family, query.expand(settings.expansionSetting()));
        } catch (Exception ignore) {}
    }

    public void startSupervising() {
        this.scheduleDelayed(() -> {
            for (RankedFamily family : this.families) {
                try {
                    int max = family.gameManager().maxAllowedPlayers();
                    int min = family.gameManager().minAllowedPlayers();
                    double variance = settings.variance();

                    if(family.playerQueue().size() < min) continue;


                    family.playerQueue().sort();
                    List<RankablePlayer> players = family.playerQueue().players();

                    if (players.size() <= max) {
                        this.handlePartition(family, family.playerQueue().players(), variance);
                        continue;
                    } // FIX RECURSSION ITS ALL BULLSHIT RN


                    PlayerRankLadder.PartitionQuery query = new PlayerRankLadder.PartitionQuery(settings.variance(), min, max);
                    this.dynamicPartition(family, query);
                } catch (Exception e) {
                    Tinder.get().logger().send(Component.text("There was a fatal error while matchmaking the family: "+family.name()));
                    e.printStackTrace();
                }
            }

            this.startSupervising();
        }, settings.interval());
    }
}
