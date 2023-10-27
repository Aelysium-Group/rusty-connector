package group.aelysium.rustyconnector.core.lib.load_balancing;

public enum AlgorithmType {
    /**
     * Used when the proxy should fill servers, in order.
     * Round robin iterates over the entire server queue, in order, placing players into the servers one by one.
     */
    ROUND_ROBIN,

    /**
     * Used when the proxy should fill the servers that have the least number of players, first.
     */
    LEAST_CONNECTION,

    /**
     * Used when the proxy should fill the servers that have the highest number of players, first.
     */
    MOST_CONNECTION
}