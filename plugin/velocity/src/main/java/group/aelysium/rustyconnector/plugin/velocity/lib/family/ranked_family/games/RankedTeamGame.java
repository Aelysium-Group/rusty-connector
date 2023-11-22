package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.util.*;

public class RankedTeamGame extends RankedGame {
    protected final Settings settings;

    protected RankedTeamGame(Settings settings, List<RankablePlayer> players) {
        this.settings = settings;
        this.teams = settings.createTeams();

        for (RankablePlayer player : players)
            for (RankedTeam team : this.teams) {
                try {
                    team.join(player);
                    break;
                } catch (Exception ignore) {}

                return; // If we loop through all teams without getting a `break;` the game is full.
            }
    }

    public static RankedTeamGame startNew(Settings settings, List<RankablePlayer> players) {
        return new RankedTeamGame(settings, players);
    }

    public record Settings(List<RankedTeam.Settings> teams, int minPlayers) {
        public List<RankedTeam> createTeams() {
            List<RankedTeam> finishedTeams = new ArrayList<>();
            this.teams.forEach(teamSettings -> {
                finishedTeams.add(new RankedTeam(teamSettings));
            });
            return finishedTeams;
        }
    }
}
