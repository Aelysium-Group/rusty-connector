package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.WinLossPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IGamemodeRankManager;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRankProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public class WinLoss extends Matchmaker {
    private final Random random = new Random();

    public WinLoss(Settings settings, StorageService storage, IGamemodeRankManager game) {
        super(settings, storage, game);
    }

    private Session.Waiting attemptBuild(int playerCount, double variance) {
        // Randomly selects a player index to use as our pivot
        int randomIndex = random.nextInt(playerCount);
        IPlayerRankProfile pivot = this.waitingPlayers.get(randomIndex);
        double pivotRank = ((WinLossPlayerRank) pivot.rank()).rank();
        double floor = pivotRank - variance;
        double ceiling = pivotRank + variance;

        // Check variance mask. Fetches all players that fit variance.
        List<IPlayerRankProfile> validPlayers = this.waitingPlayers.stream().filter(player -> {
            double rank = ((WinLossPlayerRank) player.rank()).rank();
            return rank > floor && rank < ceiling;
        }).toList();
        if(validPlayers.size() < minPlayersPerGame) return null;

        // Start building game
        Session.Builder builder = new Session.Builder();

        List<IPlayerRankProfile> playersToUse = new ArrayList<>();
        for(IPlayerRankProfile player : validPlayers) {
            try {
                builder.addPlayer(player.player().orElseThrow());
                playersToUse.add(player);
            } catch (NoSuchElementException ignore) {
                this.waitingPlayers.remove(player);
            }
        }

        this.waitingPlayers.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public Session.Waiting make() {
        if(!minimumPlayersExist()) return null;
        int playerCount = this.waitingPlayers.size(); // Calculate once so we don't keep calling it.
        boolean enoughForFullGame = playerCount > maxPlayersPerGame;

        if(!enoughForFullGame) return null;

        double variance = settings.ranking().variance() * ((WinLossPlayerRank) this.waitingPlayers.lastElement().rank()).rank(); // Variance is a percentage of the highest rank

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