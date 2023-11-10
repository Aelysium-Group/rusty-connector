package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players;

import com.mysql.cj.exceptions.NumberOutOfRange;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class PlayerRankLadder implements Service {
    protected boolean changed = false;
    protected final Vector<RankablePlayer> players = new Vector<>();

    public void add(RankablePlayer player) {
        this.changed = true;
        this.players.add(player);
    }

    public List<RankablePlayer> players() {
        return players.stream().toList();
    }

    /**
     * Gets the number of players in the queue.
     */
    public int size() {
        return this.players.size();
    }

    /**
     * Removes the player from waiting.
     * @param player {@link RankablePlayer}
     */
    public void remove(RankablePlayer player) {
        this.players.remove(player);
    }

    /**
     * Removes the players from waiting.
     * @param players {@link List<RankablePlayer>}
     */
    public void remove(List<RankablePlayer> players) {
        players.forEach(this.players::remove);
    }

    /**
     * Sort all waiting players based on their rank.
     */
    public void sort() {
        QuickSort.sort(this.players);
    }

    /**
     * Partition the players into sorted partitions that match the variance and max player laws established.
     * The returned partitions of players must still be compared against min-players to validate that they are large enough.
     * <p>
     * Partitions effectively join similar ranks together. If a partition fails a min player check it probably doesn't have a
     * wide enough variance expansion.
     * <p>
     * Partition operates by first splitting the total queued players into lists of `length = maxPlayers`.
     * It then removes any player in these lists which don't match `variance`.
     * It then removes any partitions which don't meet `length = minPlayers`.
     * Only valid partitions are returned.
     * <p>
     * It is the responsibility of the caller to decide if it needs to expand its search.
     * @param query A {@link PartitionQuery}.
     * @return A list of valid partitions. Each sub-list is considered a partition.
     * @throws NumberOutOfRange If `variance` is not a percentage represented as a decimal between 0 and 1.
     * @throws NoSuchElementException If there aren't enough players to create even a single partition.
     * @throws IndexOutOfBoundsException If no valid partitions could be found. The caller can try expanding their search to get some valid partitions.
     */
    public List<List<RankablePlayer>> partition(PartitionQuery query) throws NumberOutOfRange, NoSuchElementException, IndexOutOfBoundsException {
        if(query.variance() < 0) throw new NumberOutOfRange("Number must be a decimal between 0 and 1.");
        if(query.variance() > 1) throw new NumberOutOfRange("Number must be a decimal between 0 and 1.");
        if(this.players.size() <= query.min()) throw new NoSuchElementException("There are not enough players to create even a single partition!");

        List<RankablePlayer> playerClone = this.players();
        List<List<RankablePlayer>> partitions = new ArrayList<>();

        // If there are fewer players than what a single partition is, there's only one partition.
        if(playerClone.size() <= query.max()) {
            partitions.add(playerClone);
            return partitions;
        }

        // Remove players that don't meet variance requirements.
        int i = 0;
        do {
            int start = i * query.max();
            int end = (i * query.max()) + query.max();

            if(start >= playerClone.size()) start = playerClone.size() - 1;
            if(end >= playerClone.size()) end = playerClone.size() - 1;

            // Break the players into a single segment.
            List<RankablePlayer> partition = playerClone.subList(start, end);

            int middle = (int) Math.round(partition.size() * 0.5);
            RankablePlayer middlePlayer = partition.get(middle);
            double originScore = middlePlayer.scorecard().rating().getConservativeRating();

            // Check the segments and remove players that don't fit the middle player's rank after variance is calculated.
            partition.removeIf(player1 -> {
                        double calculatedVariance = (query.variance() * 0.5) * 50;

                        double playerScore = player1.scorecard().rating().getConservativeRating();
                        if(playerScore < originScore - calculatedVariance) return true;
                        if(playerScore > originScore + calculatedVariance) return true;
                        return false;
                    });



            partitions.add(partition);
            i++;
        } while (i * query.max() < playerClone.size());

        // Remove any partitions that are too small
        partitions.removeIf(partition -> partition.size() < query.min());

        if(partitions.size() == 0) throw new IndexOutOfBoundsException("No valid partitions could be found! Try expanding your search!");

        return partitions;
    }

    @Override
    public void kill() {
        this.players.clear();
    }

    public static class PartitionQuery {
        protected double variance;
        protected int min;
        protected int max;

        /**
         * A query used for making partition requests.
         * @param variance A percentage represented by a decimal between 0 and 1.
         * @param min The minimum number of players the partition should contain.
         * @param max The maximum number of players the partition should contain.
         */
        public PartitionQuery(double variance, int min, int max) {
            assert variance >= 0 && variance <= 1 : "variance must be a decimal between 0 and 1!";
            assert min < max : "min must be less than max!";

            this.variance = variance;
            this.min = min;
            this.max = max;
        }

        public double variance() {
            return variance;
        }

        public int min() {
            return min;
        }

        public int max() {
            return max;
        }
    }
}