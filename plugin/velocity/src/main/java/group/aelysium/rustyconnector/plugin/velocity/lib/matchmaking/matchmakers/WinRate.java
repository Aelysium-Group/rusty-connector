package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinRatePlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class WinRate extends Matchmaker {
    private final Random random = new Random();

    public WinRate(Settings settings, StorageService storage, String gameId) {
        super(settings, storage, gameId);
    }

    private Session.Waiting attemptBuild(int playerCount, double variance) {
        // Randomly selects a player index to use as our pivot
        int randomIndex = random.nextInt(playerCount);
        IMatchPlayer<IPlayerRank> pivot = this.waitingPlayers.get(randomIndex);
        double pivotRank = ((WinRatePlayerRank) pivot.rank()).rank();
        double floor = pivotRank - variance;
        double ceiling = pivotRank + variance;

        // Check variance mask. Fetches all players that fit variance.
        List<IMatchPlayer<IPlayerRank>> validPlayers = this.waitingPlayers.stream().filter(player -> {
            double rank = ((WinRatePlayerRank) player.rank()).rank();
            return rank > floor && rank < ceiling;
        }).toList();
        if(validPlayers.size() < minPlayersPerGame) return null;

        // Start building game
        Session.Builder builder = new Session.Builder();

        List<IMatchPlayer<IPlayerRank>> playersToUse = new ArrayList<>();
        for(IMatchPlayer<IPlayerRank> matchPlayer : validPlayers) {
            try {
                builder.addPlayer(matchPlayer);
                playersToUse.add(matchPlayer);
            } catch (NoSuchElementException ignore) {
                this.waitingPlayers.remove(matchPlayer);
            }
        }

        this.waitingPlayers.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public Session.Waiting make() {
        if(!minimumPlayersExist()) return null;
        int playerCount = this.waitingPlayers.size(); // Calculate once so we don't keep calling it.

        double variance = settings.ranking().variance(); // Since WinRate is a percentage, variance is exactly the value it represents.

        // Attempt to build a game 5 times. If one of the attempts succeeds, we return that and break the loop.
        for (int i = 0; i < 5; i++) {
            Session.Waiting game = attemptBuild(playerCount, variance);
            if(game != null) return game;
        }

        return null;
    }

    @Override
    public void completeSort() {
        WeightedQuickSort.sort(this.waitingPlayers);
    }
}