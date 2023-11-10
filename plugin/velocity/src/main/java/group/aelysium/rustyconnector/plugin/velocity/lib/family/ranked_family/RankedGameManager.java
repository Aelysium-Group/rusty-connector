package group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family;

import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGame;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.RankedGameRankerType;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.games.teams.RankedTeam;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.PlayerRankLadder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.players.RankablePlayer;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;

public class RankedGameManager implements Service {
    protected Vector<RankedGame> games = new Vector<>();
    protected final PlayerRankLadder waitingPlayers;
    protected final RankedMatchmakingSupervisor supervisor;
    protected final RankedFamily owner;
    protected final RankedMatchmakerSettings settings;

    public RankedGameManager(RankedMatchmakerSettings settings, RankedFamily owner) {
        this.settings = settings;
        this.waitingPlayers = new PlayerRankLadder();
        this.supervisor = new RankedMatchmakingSupervisor(settings, owner, this.waitingPlayers);
        this.owner = owner;
    }

    public RankedGameRankerType type() {
        return settings.type();
    }

    /**
     * The name representing this gamemode.
     * @return {@link String}
     */
    public String name() {
        return this.settings.name();
    }

    /**
     * A list of players waiting to join a game.
     * @return {@link PlayerRankLadder}
     */
    public PlayerRankLadder playerQueue() {
        return this.waitingPlayers;
    }

    /**
     * Registers a new game into the manager.
     * Games can only be registered if they've been assigned a server.
     * @param game The game to register.
     * @throws IllegalStateException If no server has been set on this game.
     */
    public void register(RankedGame game) throws IllegalStateException {
        if(game.server() == null) throw new IllegalStateException("Attempt to register a game that doesn't have a server!");
        this.games.add(game);
    }

    /**
     * Ends a game based on UUID.
     * This method will end the game,
     * unlock its server so other games can join it,
     * and connect all the members of the current game back to the parent family.
     * @param uuid The uuid of the game to end.
     */
    public void end(UUID uuid) {
        List<RankedGame> games = this.games.stream().filter(game -> game.uuid().equals(uuid)).toList();
        games.forEach(game -> {
            if(game.server() == null) return;
            if(game.ended()) return;

            game.end();
            owner.unlockServer(game.server());
            game.players().forEach(player -> {
                try {
                    Objects.requireNonNull(owner.parent().get()).connect(player.player());
                }catch (Exception ignore){}
            });
        });
        this.games.removeAll(games);
    }

    public static int maxAllowedPlayers(RankedMatchmakerSettings settings) {
        if(settings.teamSettings() != null) {
            int maxPlayers = 0;
            for (RankedTeam.Settings teamSettings : settings.teamSettings().teams())
                maxPlayers = maxPlayers + teamSettings.maxPlayers();

            return maxPlayers;
        }
        if(settings.soloSettings() != null)
            return settings.soloSettings().maxPlayers();

        throw new NullPointerException("No settings were defined for the game manager!");
    }
    public static int minAllowedPlayers(RankedMatchmakerSettings settings) {
        if(settings.teamSettings() != null) return settings.teamSettings().minPlayers();
        if(settings.soloSettings() != null) return settings.soloSettings().minPlayers();

        throw new NullPointerException("No settings were defined for the game manager!");
    }

    public void queue(RankablePlayer player) {
        this.waitingPlayers.add(player);
    }

    @Override
    public void kill() {

    }
}
