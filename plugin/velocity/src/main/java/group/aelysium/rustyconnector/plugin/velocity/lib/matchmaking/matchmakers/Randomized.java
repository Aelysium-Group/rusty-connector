package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;

import java.util.ArrayList;
import java.util.List;

public class Randomized extends Matchmaker<RandomizedPlayerRank> {
    public Randomized(Settings settings) {
        super(settings);
    }

    @Override
    public Session make() {
        if(!minimumPlayersExist()) return null;
        boolean enoughForFullGame = this.waitingPlayers.size() > maxPlayersPerGame;

        Session.Builder builder = new Session.Builder().teams(settings.teams());

        List<RankedPlayer<RandomizedPlayerRank>> playersToUse = new ArrayList<>();
        for(RankedPlayer<RandomizedPlayerRank> player : this.waitingPlayers) {
            if (!builder.addPlayer(player)) break;
            playersToUse.add(player);
        }

        this.waitingPlayers.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public void completeSort() {}
}