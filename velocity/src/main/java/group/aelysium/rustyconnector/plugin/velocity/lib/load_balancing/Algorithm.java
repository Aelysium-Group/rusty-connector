package group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing;

import group.aelysium.rustyconnector.core.lib.generic.load_balancing.AlgorithmType;
import group.aelysium.rustyconnector.core.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.lib.generic.server.Server;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;

import java.util.List;

public class Algorithm {
    /**
     * Looks at all the servers in the family, calculates which one the player should connect to and returns the name of that server.
     * @param family The family to look at.
     * @return The name of the server to connect to.
     */
    public List<PaperServer> balance(ServerFamily family) {
        return null;
    }

    /**
     * Return an algorithm matching the defined type
     * @param type The type of algorithm to return
     * @return The algorithm
     */
    public static Algorithm getAlgorithm(AlgorithmType type) {
        switch (type) {
            case CASCADE:
                return new CascadeAlgorithm();
            default:
                return new TroughAlgorithm();
        }
    }
}
