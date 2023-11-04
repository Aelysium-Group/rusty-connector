package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams;

import group.aelysium.rustyconnector.core.lib.algorithm.WeightedQuickSort;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

public class RankedTeamGame implements IRankedGame {
    protected List<RankedTeam> teams;
    protected final Settings settings;

    public RankedTeamGame(Settings settings, List<RankablePlayer> players) {
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

    public List<RankablePlayer> players() {
        List<RankablePlayer> players = new ArrayList<>();
        this.teams.forEach(team -> players.add((RankablePlayer) team.players()));
        return players;
    }

    /**
     * Connects a player to this ranked game.
     * @param player The player to connect.
     * @throws AccessDeniedException If there are no teams with open slots.
     * @throws NoSuchMethodException If there are no teams in this game.
     */
    public synchronized void join(RankablePlayer player) throws AccessDeniedException, NoSuchMethodException {
        if(this.teams.isEmpty()) throw new NoSuchMethodException("There are no teams in this game!");

        List<RankedTeam> teamsClone = this.teams.stream().toList();
        WeightedQuickSort.sort(teamsClone);
        this.teams = teamsClone;

        RankedTeam targetTeam = this.teams.get(0);
        if(targetTeam.full()) throw new AccessDeniedException("This game is full!");

        targetTeam.join(player);
    }

    /**
     * Removes a player from this game if they're in it.
     * @param player The player to remove.
     */
    public void leave(ResolvablePlayer player) {
        this.teams.forEach(team -> team.players().removeIf(iPlayer -> iPlayer.equals(player)));
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
