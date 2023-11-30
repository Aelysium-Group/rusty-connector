package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games;

import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.RankedPlayer;

import java.util.*;

public class RankedSoloGame extends RankedGame {
    protected final Settings settings;

    protected RankedSoloGame(Settings settings, List<RankedPlayer> players) {
        this.settings = settings;
        players.forEach(player -> {
            RankedTeam team = new RankedTeam(new RankedTeam.Settings(player.player().username(), 1));
            this.teams.add(team);
        });
    }

    public static RankedSoloGame startNew(Settings settings, List<RankedPlayer> players) {
        return new RankedSoloGame(settings, players);
    }

    public record Settings(int maxPlayers, int minPlayers) {}
}
