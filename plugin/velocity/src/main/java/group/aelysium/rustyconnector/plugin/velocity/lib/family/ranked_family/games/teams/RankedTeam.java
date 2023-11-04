package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams;

import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.Team;
import group.aelysium.rustyconnector.api.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRanker;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.nio.file.AccessDeniedException;
import java.util.List;

public class RankedTeam implements IRanker, ISortable {
    protected Settings settings;
    protected Team team = new Team();
    protected int rank = 1;

    protected RankedTeam(Settings settings) {
        this.settings = settings;
    }

    public void join(RankablePlayer player) throws AccessDeniedException {
        if(this.full()) throw new AccessDeniedException("This team is full!");
        this.team.addPlayer(player.player(), player.scorecard().rating());
    }

    public boolean full() {
        return this.playerCount() >= this.settings.maxPlayers();
    }

    public Settings settings() {
        return this.settings;
    }

    public Team team() {
        return team;
    }

    public int rank() {
        return rank;
    }

    public void updateRank(int rank) {
        this.rank = rank;
    }

    public List<? extends IPlayer> players() {
        return this.team.keySet().stream().toList();
    }

    public int playerCount() {
        return this.team.size();
    }

    @Override
    public double sortIndex() {
        // Calculates the number of open slots available
        int size = this.team.size();
        int max = this.settings.maxPlayers();
        int dif = max - size;
        return Math.max(dif, 0);
    }

    @Override
    public int weight() {
        return 0;
    }

    public record Settings(String teamName, int maxPlayers, String game) {}
}
