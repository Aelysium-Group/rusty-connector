package rustyconnector.generic.lib.generic.server;

public interface Server {
    int playerCount = 0;
    int priorityIndex = 0;
    int softPlayerCap = 20;
    int hardPlayerCap = 30;

    /**
     * Get the number of players on this server
     * @return The player count
     */
    int getPlayerCount();

    /**
     * Set the number of players on the server
     * @return The player count to set
     */
    void setPlayerCount(int playerCount);

    /**
     * Get the priority index of this server
     * @return The priority index of this server
     */
    int getPriorityIndex();

    /**
     * Get the soft player cap of this server
     * @return The soft player cap of this server
     */
    int getSoftPlayerCap();
    /**
     * Get the hard player cap of this server
     * @return The hard player cap
     */
    int getHardPlayerCap();
}
