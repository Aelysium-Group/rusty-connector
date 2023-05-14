package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

public class RoundedSessionGroupManager {
    private final Vector<RoundedSessionGroup> pendingRounds = new Vector<>();
    private final Stack<RoundedSessionGroup> readyRounds = new Stack<>();
    private final int minPlayers;
    private final int maxPlayers;

    public RoundedSessionGroupManager(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Add the player to a round.
     * If no valid round exists, create one and add the player to it.
     *
     * @param player The player.
     */
    public void joinGroup(Player player) {
        if(this.pendingRounds.size() <= 0) this.pendingRounds.add(new RoundedSessionGroup(this.minPlayers, this.maxPlayers));
        List<RoundedSessionGroup> availableRounds = this.pendingRounds.stream().filter(entry -> entry.getPlayerCount() < entry.getMaxPlayers()).toList();
        if(availableRounds.size() <= 0) {
            RoundedSessionGroup round = new RoundedSessionGroup(this.minPlayers, this.maxPlayers);
            round.add(player);
            this.pendingRounds.add(round);

            return;
        }
        RoundedSessionGroup round = availableRounds.get(0);
        round.add(player);
    }

    /**
     * Remove the player from their round.
     * @param player The player.
     */
    public void leaveGroup(Player player) {
        List<RoundedSessionGroup> results = this.pendingRounds.stream().filter(entry -> entry.contains(player)).toList();
        results.forEach(round -> round.remove(player));
    }

    /**
     * Move any valid rounds into the queue to be put into session next.
     */
    public void queueValidGroups() {
        List<RoundedSessionGroup> validRounds = this.pendingRounds.stream().filter(entry -> entry.getPlayerCount() >= entry.getMinPlayers()).toList();
        for (RoundedSessionGroup group : validRounds) {
            this.pendingRounds.remove(group);
            this.readyRounds.add(group);
        }
    }

    /**
     * Return the next round that is queued for a session.
     * Once the round is returned, it is removed from the queue.
     * @return The round. Or `null` if there's no rounds in the queue.
     */
    public RoundedSessionGroup popFromSessionQueue() {
        try {
            RoundedSessionGroup group = this.readyRounds.pop();

            return group;
        } catch (Exception ignore) {
            return null;
        }
    }

    public int getPendingGroups() {
        return this.pendingRounds.size();
    }

    public int getReadyGroups() {
        return this.readyRounds.size();
    }
}
