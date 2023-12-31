package group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay;

import group.aelysium.rustyconnector.toolkit.core.JSONParseable;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.rmi.AlreadyBoundException;
import java.util.List;
import java.util.UUID;

public interface ISession extends JSONParseable {
    UUID uuid();

    /**
     * Ends this session.
     */
    void end();

    IRankedMCLoader mcLoader();

    /**
     * Gets the players that are currently in this session.
     */
    List<IRankedPlayer> players();


    interface IWaiting {
        UUID uuid();

        /**
         * Starts the session on the specified MCLoader.
         * By the time {@link ISession} is returned, it should be assumed that all players have connected.
         * @param mcLoader The MCLoader to run the session on.
         * @return A running {@link ISession}.
         * @throws AlreadyBoundException If a session is already running on this MCLoader.
         */
        ISession start(IRankedMCLoader mcLoader) throws AlreadyBoundException;
    }
}
