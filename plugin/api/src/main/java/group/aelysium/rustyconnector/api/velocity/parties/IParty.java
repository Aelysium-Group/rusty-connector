package group.aelysium.rustyconnector.api.velocity.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.server.IPlayerServer;
import net.kyori.adventure.text.Component;

import java.util.Vector;

public interface IParty<TPlayerServer extends IPlayerServer> {
    /**
     * Sets the server that this party is assigned to.
     * <p>
     * Setting the server with this method does not cause the party to connect to that new server.
     * If you'd like to connect the party to a new server, you can use {@link IParty#connect(IPlayerServer)}.
     * This will automatically connect the party to the new server and also set the party's server to the new server.
     * @param server The server to assign to this party.
     */
    void setServer(TPlayerServer server);

    /**
     * Gets the server that this party has been assigned to.
     * @return {@link IPlayerServer}
     */
    TPlayerServer server();

    /**
     * Gets the leader of this party.
     * If the leader is no longer a member of the party, or if they aren't online anymore, this method will call {@link IParty#newRandomLeader()} and assign a new leader.
     * @return {@link Player}
     * @throws IllegalStateException If the party is empty.
     */
    Player leader() throws IllegalStateException;

    /**
     * Sets a player as the leader of this party.
     * The player must already be a member of the party before they can be promoted.
     * If you aren't sure if the player is already a member of the party, try:
     * <pre>
     *     if(party.contains(player))
     *          party.setLeader(player);
     * </pre>
     * The previous leader of this party will be demoted to just a regular member.
     * @param player The player to set as leader.
     * @throws IllegalStateException If the player isn't a member of the party.
     */
    void setLeader(Player player) throws IllegalStateException;

    /**
     * Effectively set a new leader.
     * Method will generate a {@link java.util.Random random number} between `0` and `{@link IParty#players()}.{@link Vector#size()} - 1`.
     * It will then fetch the player at `index = random`.
     * If there's an issue fetching the player, it will simply fetch the first player in {@link IParty#players()}.
     * <p>
     * The selected player becomes leader.
     * @throws IllegalStateException If the party is empty.
     */
    void newRandomLeader() throws IllegalStateException;

    /**
     * Gets if the party is empty or not.
     * Empty parties are automatically closed and destroyed.
     * This is a convenience method to help prevent access to parties that are worthless and queued for destruction in multi-threaded environments.
     * @return `true` if the party is empty. `false` otherwise.
     */
    boolean isEmpty();

    /**
     * Gets a vector of all the players in this party.
     * @return {@link Vector<Player>}
     */
    Vector<Player> players();

    /**
     * Adds the player to this party.
     * This method does not connect the player to the party's server. You'll need to use {@link IParty#server()}.{@link IPlayerServer#directConnect(Player) directConnect(Player)}.
     * However, once a player is in a party, they will be pulled into the next server the party travels to.
     * @param player The player to join the party.
     * @throws IllegalStateException If the party is empty. Empty parties are marked as "destroyable" and get decomposed. You'll have to create a new party and connect the player to that one instead.
     * @throws RuntimeException If the party is full. Checks {@link IParty#players()}.{@link Vector#size()} against {@link PartyServiceSettings#maxMembers()}.
     */
    void join(Player player) throws IllegalStateException, RuntimeException;

    /**
     * Removes the player from this party.
     * If this party is already empty, this method will fail silently.
     * <p>
     * If this player is the last member of the party, this will disband the party and it will decompose.
     * If that's the case, all future calls to this party will throw {@link IllegalStateException}.
     * <p>
     * If this player was the leader of this party (but not the last member) this method will call {@link IParty#newRandomLeader()} and assign a new, random leader.
     * @param player The player to leave.
     */
    void leave(Player player);

    /**
     * Broadcast a message to all members of the party.
     * @param message The message to send.
     */
    void broadcast(Component message);

    /**
     * Checks if the player is a member of the party.
     * @param player The player to check for.
     * @return {@link Boolean}
     */
    boolean contains(Player player);

    /**
     * Decomposes the party, removing its data and making it unusable.
     */
    void decompose();

    /**
     * This method connects the entire party to the specified server.
     * It also calls {@link IParty#setServer(IPlayerServer)}, setting the passed server as this party's new server.
     * <p>
     * If this player is unable to join the server for some reason, they will be kicked from the party via {@link IParty#leave(Player)} and receive an error message.
     * @param server The server to connect to.
     */
    void connect(TPlayerServer server);
}
