package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games;

import de.gesundkrank.jskills.IPlayer;
import de.gesundkrank.jskills.Team;
import group.aelysium.rustyconnector.toolkit.velocity.load_balancing.ISortable;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRanker;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;

import java.nio.file.AccessDeniedException;
import java.util.List;

public class RankedTeam implements IRanker, ISortable {
    protected Settings settings;
    protected Team team = new Team();
    protected int rank = 1;

    public RankedTeam(Settings settings) {
        this.settings = settings;
    }

    public void join(RankedPlayer player) throws AccessDeniedException {
        if(this.full()) throw new AccessDeniedException("This team is full!");
    }

    public boolean full() {
        return this.playerCount() >= this.settings.maxPlayers();
    }

    public Settings settings() {
        return this.settings;
    }

    public Team innerTeam() {
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

    public record Settings(String teamName, int maxPlayers) {}
}