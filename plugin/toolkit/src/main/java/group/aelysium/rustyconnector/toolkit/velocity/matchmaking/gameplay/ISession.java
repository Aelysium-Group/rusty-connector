package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay;

import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.player_rank.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;

import java.util.*;

public interface ISession<TMCLoader extends IMCLoader> {
    /**
     * Connects the session to a server.
     * @param server The server to connect to.
     */
    void connect(TMCLoader server);

    /**
     * Ends the current session.
     * This method will disconnect all players from the session's server and return them to the parent family.
     */
    void end();

    /**
     * Gets all the players currently in this team.
     * @return {@link List<IRankedPlayer>}
     */
    List<? extends IRankedPlayer<? extends IPlayer, ? extends IPlayerRank<?>>> players();
}
