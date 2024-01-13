package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.matchmakers;

import group.aelysium.rustyconnector.core.lib.algorithm.SingleSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay.Session;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.player_rank.RandomizedPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.connection.ConnectionResult;
import group.aelysium.rustyconnector.toolkit.velocity.connection.PlayerConnectable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

public class Randomized extends Matchmaker {
    public Randomized(Settings settings) {
        super(settings);
    }

    @Override
    public void add(PlayerConnectable.Request request, CompletableFuture<ConnectionResult> result) {
        try {
            IRankedPlayer rankedPlayer = new RankedPlayer(request.player().uuid(), new RandomizedPlayerRank());

            if (this.settings.reconnect()) {
                for (ISession session : this.runningSessions.values().stream().toList()) {
                    for (IPlayer player : session.players()) {
                        if (player.uuid() == rankedPlayer.uuid()) {
                            session.mcLoader().connect(player);
                            result.complete(ConnectionResult.success(Component.text("You've been reconnected to your game."), session.mcLoader()));
                            return;
                        }
                    }
                }
            }

            if(this.waitingPlayers.contains(rankedPlayer)) throw new RuntimeException("Player is already queued!");

            this.waitingPlayers.add(rankedPlayer);

            if(this.waitingPlayers.size() >= 2) try {
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

        List<IRankedPlayer> playersToUse = new ArrayList<>();
        for(IRankedPlayer player : this.waitingPlayers) {
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
    public void completeSort() {}
}