package group.aelysium.rustyconnector.core.lib.model;

public interface Server {

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
     * Get the weight of this server
     * @return The weight of this server
     */
    int getWeight();

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
