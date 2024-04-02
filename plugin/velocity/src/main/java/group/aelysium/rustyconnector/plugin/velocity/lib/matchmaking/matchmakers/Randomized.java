package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.MatchPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IMatchPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class Randomized extends Matchmaker {
    public Randomized(Settings settings, StorageService storage, String gameId) {
        super(settings, storage, gameId);
    }

    @Override
    public void add(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result) {
        try {
            IMatchPlayer<IPlayerRank> matchPlayer = new MatchPlayer<>(request.player(), new RandomizedPlayerRank(), this.gameId);

            if (this.settings.queue().joining().reconnect())
                for (ISession session : this.runningSessions.values()) {
                    if(!session.contains(matchPlayer)) continue;

                    PlayerConnectable.Request request1 = session.mcLoader().connect(matchPlayer.player());
                    request1.wait(10000);
                    result.complete(ConnectionResult.success(Component.text("You've been reconnected to your game."), session.mcLoader()));
                    return;
                }

            if(this.waitingPlayers.contains(matchPlayer)) throw new RuntimeException("Player is already queued!");

            this.waitingPlayers.add(matchPlayer);

            if(minimumPlayersExist()) try {
                int index = this.waitingPlayers.size() - 1;
                SingleSort.sort(this.waitingPlayers, index);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            result.complete(ConnectionResult.failed(Component.text("There was an issue queuing into matchmaking!")));
            throw new RuntimeException(e);
        }
        result.complete(ConnectionResult.success(Component.text("Successfully queued into the matchmaker!"), null));
    }

    @Override
    public Session.Waiting make() {
        if(!minimumPlayersExist()) return null;

        Session.Builder builder = new Session.Builder();

        List<IMatchPlayer<IPlayerRank>> playersToUse = new ArrayList<>();
        for(IMatchPlayer<IPlayerRank> matchPlayer : this.waitingPlayers) {
            try {
                builder.addPlayer(matchPlayer);
                playersToUse.add(matchPlayer);
            } catch (NoSuchElementException ignore) { // Removes the player because they don't seem to exist
                this.waitingPlayers.remove(matchPlayer);
            }
            if(playersToUse.size() >= settings.session().building().max()) break;
        }

        this.waitingPlayers.removeAll(playersToUse); // Remove these players from the matchmaker

        return builder.build();
    }

    @Override
    public void completeSort() {}
}