package group.aelysium.rustyconnector.core.lib.generic.load_balancing;

import group.aelysium.rustyconnector.core.lib.generic.server.Family;
import group.aelysium.rustyconnector.core.lib.generic.server.Server;

public class Algorithm {
    /**
     * Looks at all the servers in the family, calculates which one the player should connect to and returns the name of that server.
     * @param family The family to look at.
     * @return The name of the server to connect to.
     */
    public Server processConnection(Family family) {
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
