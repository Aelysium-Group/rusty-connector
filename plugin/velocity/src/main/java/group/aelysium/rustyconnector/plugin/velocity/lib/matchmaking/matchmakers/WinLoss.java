package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinLossPlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WinLoss extends Matchmaker<WinLossPlayerRank> {
    private final Random random = new Random();

    public WinLoss(Settings settings) {
        super(settings);
    }

    private Session attemptBuild(int playerCount, double variance) {
        // Randomly selects a player index to use as our pivot
        int randomIndex = random.nextInt(playerCount);
        RankedPlayer<WinLossPlayerRank> pivot = this.items.get(randomIndex);
        double pivotRank = pivot.rank().rank();
        double floor = pivotRank - variance;
        double ceiling = pivotRank + variance;

        // Check variance mask. Fetches all players that fit variance.
        List<RankedPlayer<WinLossPlayerRank>> validPlayers = this.items.stream().filter(player -> {
            double rank = player.rank().rank();
            return rank > floor && rank < ceiling;
        }).toList();
        if(validPlayers.size() < minPlayersPerGame) return null;

        // Start building game
        Session.Builder builder = new Session.Builder().teams(settings.teams());

        List<RankedPlayer<WinLossPlayerRank>> playersToUse = new ArrayList<>();
        for(RankedPlayer<WinLossPlayerRank> player : validPlayers) {
            if (!builder.addPlayer(player)) break;
            playersToUse.add(player);
        }

        this.items.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public Session make() {
        if(!minimumPlayersExist()) return null;
        int playerCount = this.items.size(); // Calculate once so we don't keep calling it.
        boolean enoughForFullGame = playerCount > maxPlayersPerGame;

        if(enoughForFullGame) return null;

        double variance = settings.variance() * this.items.lastElement().rank().rank(); // Variance is a percentage of the highest rank

        // Attempt to build a game 5 times. If one of the attempts succeeds, we return that and break the loop.
        for (int i = 0; i < 5; i++) {
            Session game = attemptBuild(playerCount, variance);
            if(game != null) return game;
        }

        return null;
    }

    @Override
    public void completeSort() {
        WeightedQuickSort.sort(this.items);
    }
}