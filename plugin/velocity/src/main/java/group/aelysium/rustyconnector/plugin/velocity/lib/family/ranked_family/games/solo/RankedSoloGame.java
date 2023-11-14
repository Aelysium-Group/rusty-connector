package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo;

import com.velocitypowered.api.proxy.Player;
import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.ITeam;
import de.gesundkrank.jskills.Team;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.rmi.ConnectException;
import java.util.*;

public class RankedSoloGame extends RankedGame {
    protected final Team players = new Team();
    protected final Settings settings;

    protected RankedSoloGame(Settings settings, List<RankablePlayer> players) {
        this.settings = settings;
        players.forEach(player -> {
            this.players.addPlayer(player, player.scorecard().rating());
        });
    }

    public List<RankablePlayer> players() {
        List<RankablePlayer> players = new ArrayList<>();
        this.players.keySet().forEach(player -> players.add((RankablePlayer) player));
        return players;
    }

    protected <TTeam extends ITeam> Collection<TTeam> teams() {
        Collection<TTeam> teams = new ArrayList<>();
        this.players.forEach((key, value) -> teams.add((TTeam) (new Team()).addPlayer(key, value)));
        return teams;
    }

    public UUID uuid() {
        return null;
    }

    @Override
    public void connectServer(PlayerServer server) {
        Vector<Player> kickedPlayers = new Vector<>();

        for (RankablePlayer rankablePlayer : this.players()) {
            try {
                Player player = rankablePlayer.player().resolve().orElseThrow();
                try {
                    server.directConnect(player);
                } catch (ConnectException e) {
                    kickedPlayers.add(player);
                }
            } catch (NoSuchElementException ignore) {
            }
        }

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
