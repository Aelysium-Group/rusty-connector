package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.rmi.ConnectException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.Vector;

public class RankedSoloGame extends RankedGame {
    protected final List<RankablePlayer> players;
    protected final Settings settings;

    protected RankedSoloGame(Settings settings, List<RankablePlayer> players) {
        this.settings = settings;
        this.players = players;
    }

    public List<RankablePlayer> players() {
        return this.players;
    }

    public UUID uuid() {
        return null;
    }

    @Override
    public void connectServer(PlayerServer server) {
        Vector<RankablePlayer> removedPlayers = new Vector<>();
        Vector<Player> kickedPlayers = new Vector<>();

        for (RankablePlayer rankablePlayer : this.players) {
            try {
                Player player = rankablePlayer.player().resolve().orElseThrow();
                try {
                    server.directConnect(player);
                } catch (ConnectException e) {
                    kickedPlayers.add(player);
                }
            } catch (NoSuchElementException ignore) {
                removedPlayers.add(rankablePlayer);
            }
        }

        this.players.removeAll(removedPlayers);

        kickedPlayers.forEach(player -> {
            player.sendMessage(VelocityLang.GAME_FOLLOW_KICKED);
        });

        super.connectServer(server);
    }

    public static RankedSoloGame startNew(Settings settings, List<RankablePlayer> players) {
        return new RankedSoloGame(settings, players);
    }

    public record Settings(int maxPlayers, int minPlayers) {}
}
