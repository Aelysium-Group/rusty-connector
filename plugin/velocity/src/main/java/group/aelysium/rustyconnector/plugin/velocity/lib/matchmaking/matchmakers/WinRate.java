package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinRatePlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WinRate extends Matchmaker<WinRatePlayerRank> {
    private final Random random = new Random();

    public WinRate(Settings settings) {
        super(settings);
    }

    private Session attemptBuild(int playerCount, double variance) {
        // Randomly selects a player index to use as our pivot
        int randomIndex = random.nextInt(playerCount);
        RankedPlayer<WinRatePlayerRank> pivot = this.waitingPlayers.get(randomIndex);
        double pivotRank = pivot.rank().rank();
        double floor = pivotRank - variance;
        double ceiling = pivotRank + variance;

        // Check variance mask. Fetches all players that fit variance.
        List<RankedPlayer<WinRatePlayerRank>> validPlayers = this.waitingPlayers.stream().filter(player -> {
            double rank = player.rank().rank();
            return rank > floor && rank < ceiling;
        }).toList();
        if(validPlayers.size() < minPlayersPerGame) return null;

        // Start building game
        Session.Builder builder = new Session.Builder().teams(settings.teams());

        List<RankedPlayer<WinRatePlayerRank>> playersToUse = new ArrayList<>();
        for(RankedPlayer<WinRatePlayerRank> player : validPlayers) {
            if (!builder.addPlayer(player)) break;
            playersToUse.add(player);
        }

        this.waitingPlayers.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public Session make() {
        if(!minimumPlayersExist()) return null;
        int playerCount = this.waitingPlayers.size(); // Calculate once so we don't keep calling it.
        boolean enoughForFullGame = playerCount > maxPlayersPerGame;

        if(enoughForFullGame) return null;

        double variance = settings.variance(); // Since WinRate is a percentage, variance is exactly the value it represents.

        // Attempt to build a game 5 times. If one of the attempts succeeds, we return that and break the loop.
        for (int i = 0; i < 5; i++) {
            Session game = attemptBuild(playerCount, variance);
            if(game != null) return game;
        }

        return null;
    }

    @Override
    public void completeSort() {
        WeightedQuickSort.sort(this.waitingPlayers);
    }
}