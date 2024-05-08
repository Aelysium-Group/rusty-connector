package group.aelysium.rustyconnector.toolkit.velocity.parties;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.Vector;

public interface IParty {
    /**
     * Gets the server that this party has been assigned to.
     * @return {@link IMCLoader}
     */
    IMCLoader server();

    /**
     * Gets the leader of this party.
     * If the leader is no longer a member of the party, or if they aren't online anymore, this method will call {@link IParty#randomPlayer()} and assign a new leader.
     * @return {@link IPlayer}
     * @throws IllegalStateException If the party is empty.
     */
    IPlayer leader() throws IllegalStateException;

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
    void setLeader(IPlayer player) throws IllegalStateException;

    /**
     * Effectively set a new leader.
     * Method will generate a {@link java.util.Random random number} between `0` and `{@link IParty#players()}.{@link Vector#size()} - 1`.
     * It will then fetch the player at `index = random`.
     * If there's an issue fetching the player, it will simply fetch the first player in {@link IParty#players()}.
     * <p>
     * The selected player becomes leader.
     * @throws IllegalStateException If the party is empty.
     */
    IPlayer randomPlayer() throws IllegalStateException;

    /**
     * Gets if the party is empty or not.
     * Empty parties are automatically closed and destroyed.
     * This is a convenience method to help prevent access to parties that are worthless and queued for destruction in multi-threaded environments.
     * @return `true` if the party is empty. `false` otherwise.
     */
    boolean isEmpty();

    /**
     * Gets a vector of all the players in this party.
     * @return {@link List<IPlayer>}
     */
    List<IPlayer> players();

    /**
     * Adds the player to this party.
     * This method does not connect the player to the party's server. You'll need to use {@link IParty#server()}.{@link IMCLoader#connect(IPlayer)} connect(Player)}.
     * However, once a player is in a party, they will be pulled into the next server the party travels to.
     * @param player The player to join the party.
     * @throws IllegalStateException If the party is empty. Empty parties are marked as "destroyable" and get decomposed. You'll have to create a new party and connect the player to that one instead.
     * @throws RuntimeException If the party is full. Checks {@link IParty#players()}.{@link Vector#size()} against {@link PartyServiceSettings#maxMembers()}.
     */
    void join(IPlayer player) throws IllegalStateException, RuntimeException;

    /**
     * Removes the player from this party.
     * If this party is already empty, this method will fail silently.
     * <p>
     * If this player is the last member of the party, this will disband the party and it will decompose.
     * If that's the case, all future calls to this party will throw {@link IllegalStateException}.
     * <p>
     * If this player was the leader of this party (but not the last member) this method will call {@link IParty#randomPlayer()} and assign a new, random leader.
     * @param player The player to leave.
     */
    void leave(IPlayer player);

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
    boolean contains(IPlayer player);

    /**
     * Decomposes the party, removing its data and making it unusable.
     */
    void decompose();
}
