package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.solo;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.IRankedGame;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.util.List;

public class RankedSoloGame implements IRankedGame {
    protected final List<RankablePlayer> players;
    protected final Settings settings;

    public RankedSoloGame(Settings settings, List<RankablePlayer> players) {
        this.settings = settings;
        this.players = players;
    }

    public List<RankablePlayer> players() {
        return this.players;
    }

    public record Settings(int maxPlayers, int minPlayers) {}
}
