package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.GameMatch;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ILoadBalancer;

import java.util.ArrayList;
import java.util.List;

public class Randomized extends MatchMaker<RandomizedPlayerRank> {
    public Randomized(Settings settings) {
        super(settings);
    }

    @Override
    public GameMatch make() {
        if(!minimumPlayersExist()) return null;
        boolean enoughForFullGame = this.items.size() > maxPlayersPerGame;

        GameMatch.Builder builder = new GameMatch.Builder().teams(settings.teams());

        List<RankedPlayer<RandomizedPlayerRank>> playersToUse = new ArrayList<>();
        for(RankedPlayer<RandomizedPlayerRank> player : this.items) {
            if (!builder.addPlayer(player)) break;
            playersToUse.add(player);
        }

        this.items.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public void completeSort() {}

    @Override
    public void singleSort() {}
}