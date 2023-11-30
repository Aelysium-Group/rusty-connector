package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class GameMatch {
    protected final List<Team> teams;

    private GameMatch() {
        this.teams = new ArrayList<>();
    }
    protected GameMatch(List<Team> teams) {
        this.teams = teams;
    }

    public static class Builder {
        protected List<Team> teams = new ArrayList<>();

        public Builder teams(List<Team.Settings> settings) {
            settings.forEach(team -> this.teams.add(new Team(team, new Vector<>())));

            return this;
        }

        /**
         * Add a player to the match
         * @param player The player to add.
         * @return `true` if the player was added successfully. `false` otherwise.
         */
        public boolean addPlayer(RankedPlayer player) {
            for (Team team : teams)
                if(team.add(player)) return true;

            return false;
        }

        /**
         * Builds the gamematch.
         * @return A {@link GameMatch}, or `null` if there are still teams that aren't at least filled to the minimum.
         */
        public GameMatch build() {
            for (Team team : teams)
                if(!team.satisfactory()) return null;

            return new GameMatch(teams);
        }
    }
}
