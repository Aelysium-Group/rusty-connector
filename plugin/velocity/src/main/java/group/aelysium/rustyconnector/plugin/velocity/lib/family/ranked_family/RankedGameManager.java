package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameRankerType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo.RankedSoloGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeam;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeamGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.PlayerRankLadder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.util.List;
import java.util.Vector;

public class RankedGameManager implements Service {
    protected Vector<IRankedGame> games = new Vector<>();
    protected final PlayerRankLadder waitingPlayers;
    protected RankedMatchmakerSettings settings;

    public RankedGameManager(RankedMatchmakerSettings settings, PlayerRankLadder waitingPlayers) {
        this.settings = settings;
        this.waitingPlayers = waitingPlayers;
    }

    public RankedGameRankerType type() {
        return settings.type();
    }

    public IRankedGame start(List<RankablePlayer> players) {
        IRankedGame game = null;
        if(settings.soloSettings() != null) {
            game = new RankedSoloGame(settings.soloSettings(), players);
            this.waitingPlayers.remove(((RankedSoloGame) game).players());
        }
        if(settings.teamSettings() != null) {
            game = new RankedTeamGame(settings.teamSettings(), players);
            this.waitingPlayers.remove(((RankedTeamGame) game).players());
        }

        if(game == null) throw new NullPointerException("Unable to create a new game!");

        return game;
    }

    public int maxAllowedPlayers() {
        if(this.settings.teamSettings() != null) {
            int maxPlayers = 0;
            for (RankedTeam.Settings teamSettings : this.settings.teamSettings().teams())
                maxPlayers = maxPlayers + teamSettings.maxPlayers();

            return maxPlayers;
        }
        if(this.settings.soloSettings() != null)
            return this.settings.soloSettings().maxPlayers();

        throw new NullPointerException("No settings were defined for the game manager!");
    }
    public int minAllowedPlayers() {
        if(this.settings.teamSettings() != null) return settings.teamSettings().minPlayers();
        if(this.settings.soloSettings() != null) return settings.soloSettings().minPlayers();

        throw new NullPointerException("No settings were defined for the game manager!");
    }

    public void queue(RankablePlayer player) {
        this.waitingPlayers.add(player);
    }

    @Override
    public void kill() {

    }
}
