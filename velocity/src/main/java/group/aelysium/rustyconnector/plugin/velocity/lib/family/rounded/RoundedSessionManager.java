package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

import com.velocitypowered.api.proxy.Player;

import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;

public class RoundedSessionManager {
    private final Vector<RoundedSession> pendingSessions = new Vector<>();
    private final Stack<RoundedSession> readySessions = new Stack<>();
    private final int minPlayers;
    private final int maxPlayers;

    public RoundedSessionManager(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    /**
     * Add the player to a session.
     * If no valid round exists, create one and add the player to it.
     *
     * @param player The player.
     */
    public void join(Player player) {
        if(this.pendingSessions.size() <= 0) this.pendingSessions.add(new RoundedSession(this.minPlayers, this.maxPlayers));
        List<RoundedSession> availableSessions = this.pendingSessions.stream().filter(entry -> entry.getPlayerCount() < entry.getMaxPlayers()).toList();
        if(availableSessions.size() <= 0) {
            RoundedSession session = new RoundedSession(this.minPlayers, this.maxPlayers);
            session.add(player);
            this.pendingSessions.add(session);

            return;
        }
        RoundedSession session = availableSessions.get(0);
        session.add(player);
    }

    /**
     * Remove the player from their round.
     * @param player The player.
     */
    public void leave(Player player) {
        List<RoundedSession> results = this.pendingSessions.stream().filter(entry -> entry.contains(player)).toList();
        results.forEach(round -> round.remove(player));
    }

    /**
     * Move any valid rounds into the queue to be put into session next.
     */
    public void queueValidGroups() {
        List<RoundedSession> validRounds = this.pendingSessions.stream().filter(entry -> entry.getPlayerCount() >= entry.getMinPlayers()).toList();
        for (RoundedSession group : validRounds) {
            this.pendingSessions.remove(group);
            this.readySessions.add(group);
        }
    }

    /**
     * Return the next round that is queued for a session.
     * Once the round is returned, it is removed from the manager and can no-longer be accessed.
     * @return The round. Or `null` if there's no rounds in the queue.
     */
    public RoundedSession popSessionFromQueue() {
        try {
            return this.readySessions.pop();
        } catch (Exception ignore) {}

        return null;
    }

    public int getPendingGroups() {
        return this.pendingSessions.size();
    }

    public int getReadyGroups() {
        return this.readySessions.size();
    }
}
