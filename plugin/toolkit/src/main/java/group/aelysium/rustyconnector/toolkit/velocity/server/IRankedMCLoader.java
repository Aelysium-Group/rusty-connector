package group.aelysium.rustyconnector.toolkit.velocity.server;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.ISession;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public interface IRankedMCLoader extends IMCLoader {
    /**
     * Gets the currently active session that's on this MCLoader.
     * @return {@link Optional<ISession>}
     */
    Optional<ISession> currentSession();

    class Reference extends group.aelysium.rustyconnector.toolkit.velocity.util.Reference<IRankedMCLoader, UUID> {
        public Reference(UUID uuid) {
            super(uuid);
        }

        public <TRankedMCLoader extends IRankedMCLoader> TRankedMCLoader get() throws NoSuchElementException {
            IMCLoader server = RustyConnector.Toolkit.proxy().orElseThrow().services().server().fetch(this.referencer).orElseThrow();
            if(!(server instanceof IRankedMCLoader)) throw new NoSuchElementException();
            return (TRankedMCLoader) server;
        }
    }
}
