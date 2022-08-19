package rustyconnector.generic.lib.generic.server;

import rustyconnector.generic.lib.generic.whitelist.Whitelist;

public class Server {
    private String name;
    private int priorityIndex = 0;
    private int softPlayerCap = 20;
    private int hardPlayerCap = 30;

    public Server(String name) {
        this.name = name;
    }

    /**
     * Connect player to this server
     */
    public void connectPlayer() {}
}
