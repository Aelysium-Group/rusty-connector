package group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.gameplay;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RankedMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.rmi.AlreadyBoundException;
import java.util.*;

public class Session implements ISession {
    protected final UUID uuid;
    protected final List<IRankedPlayer> players;
    protected IRankedMCLoader mcLoader;

    private Session(UUID uuid, List<IRankedPlayer> players, IRankedMCLoader mcLoader) {
        this.uuid = uuid;
        this.players = players;
        this.mcLoader = mcLoader;
    }

    public UUID uuid() {
        return this.uuid;
    }

    public IRankedMCLoader mcLoader() {
        return this.mcLoader;
    }

    public void end() {
        RankedFamily family = (RankedFamily) this.mcLoader.family();

        family.matchmaker().remove(this);
        Family parent = family.parent();

        for (IRankedPlayer player : this.players()) {
            try {
                parent.connect(player.player().orElseThrow());
            } catch (Exception ignore) {}
        }

        this.mcLoader.unlock();
    }

    public List<IRankedPlayer> players() {
        return players;
    }

    @Override
    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("uuid", new JsonPrimitive(this.uuid.toString()));

        JsonArray array = new JsonArray();
        players.forEach(player -> array.add(player.uuid().toString()));
        object.add("players", array);

        return object;
    }

    public static class Builder {
        protected List<IRankedPlayer> players = new ArrayList<>();
        protected RankedMCLoader mcLoader;

        /**
         * Add a player to the match
         * @param player The player to add.
         */
        public void addPlayer(IRankedPlayer player) {
            this.players.add(player);
        }

        /**
         * Builds the gamematch.
         * @return A {@link Session}, or `null` if there are still teams that aren't at least filled to the minimum.
         */
        public Session.Waiting build() {
            return new Session.Waiting(players);
        }
    }

    public static class Waiting implements ISession.IWaiting {
        protected UUID uuid = UUID.randomUUID();
        protected List<IRankedPlayer> players;

        protected Waiting(List<IRankedPlayer> teams) {
            this.players = teams;
        }

        public UUID uuid() {
            return this.uuid;
        }

        /**
         * Starts the session on the specified MCLoader.
         * By the time {@link Session} is returned, it should be assumed that all players have connected.
         * @param mcLoader The MCLoader to run the session on.
         * @return A running {@link Session}.
         * @throws AlreadyBoundException If a session is already running on this MCLoader.
         */
        public ISession start(IRankedMCLoader mcLoader) {
            ISession session = new Session(uuid, players, mcLoader);
            ((RankedMCLoader) mcLoader).connect(session);
            return session;
        }
    }
}
