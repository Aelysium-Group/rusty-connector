package group.aelysium.rustyconnector.core.lib.model;

public interface ServerManager<S, F> {
    /**
     * Finds a server which has been saved to this manager.
     * @param name The name of the server to look for
     */
    S find(String name);

    /**
     * Creates a new server which can be registered to the proxy.
     * @param server The server to register to this manager.
     */
    void add(S server);

    /**
     * Register a server to the network
     * @param server The server to register
     */
    void registerServer(S server, F family);

    /**
     * Unregister a server from the network
     * @param server
     */
    void unregisterServer(S server);
}
