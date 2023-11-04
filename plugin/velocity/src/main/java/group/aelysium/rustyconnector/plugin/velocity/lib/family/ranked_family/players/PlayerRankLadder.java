package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players;

import de.gesundkrank.jskills.Rating;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.core.lib.algorithm.QuickSort;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class PlayerRankLadder implements Service {
    private static int MAX_RANK = 50;
    private static int MIN_RANK = 0;

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
     * @return A list of partitions. Each sub-list is considered a partition.
     */
    public List<List<RankablePlayer>> partition(double variance, int maxPlayers) {
        List<List<RankablePlayer>> partitions = new ArrayList<>();
        List<RankablePlayer> playerClone = this.players();

        if(playerClone.size() <= maxPlayers) {
            partitions.add(playerClone);
            return partitions;
        }

        int i = 0;
        for (RankablePlayer player : playerClone) {
            int start = i * maxPlayers;
            int end = (i * maxPlayers) + maxPlayers;

            if(start >= playerClone.size()) start = playerClone.size() - 1;
            if(end >= playerClone.size()) end = playerClone.size() - 1;

            List<RankablePlayer> prePartition = playerClone.subList(start, end);

            List<RankablePlayer> postPartition = prePartition
                    .removeIf(player1 -> );

            partitions.add(postPartition);
            i++;
        }
    }

    @Override
    public void kill() {
        this.players.clear();
    }
}
