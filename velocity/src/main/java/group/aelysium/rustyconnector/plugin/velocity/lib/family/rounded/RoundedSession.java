package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.RoundedServer;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Consumer;

public class RoundedSession {
    private final UUID sessionID = UUID.randomUUID();
    private final Vector<Player> players = new Vector<>();

    /*
     * RoundedServer also refers to it's currently active RoundedSession.
     * This could theoretically create a reference loop where the GC can't collect the server or session.
     *
     * This WeakReference is intended to prevent this.
     */
    private WeakReference<RoundedServer> server = null;
    private final int minPlayers;
    private final int maxPlayers;

    public RoundedSession(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public void assignServer(RoundedServer server) throws IllegalStateException {
        if(this.server != null) throw new IllegalStateException("You can't set the server if one is already set!");
        this.server = new WeakReference<>(server);
    }

    public UUID getUUID() { return this.sessionID; }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public List<Player> getPlayers() { return this.players.stream().toList(); }

    public RoundedServer getServer() { return this.server.get(); }

    public boolean add(Player player) {
        if(this.players.contains(player)) return true;
        return this.players.add(player);
    }

    public boolean remove(Player player) {
        return this.players.remove(player);
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    public void forEach(Consumer<? super Player> action) {
        this.players.forEach(action);
    }

    /**
     * Connect the session to its server.
     * Requires that a server has been assigned.
     */
    public boolean connect() {
        if(this.server.get() == null) throw new IllegalStateException("You must assign a server to this session before you can connect to it!");

        try {
            this.players.forEach(player -> {
                ConnectionRequestBuilder connection = player.createConnectionRequest(Objects.requireNonNull(this.server.get()).getRegisteredServer());
                try {
                    connection.connect().get().isSuccessful();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Decomposes the session so that it can be cleanly deleted.
     */
    public void decompose() {
        this.players.clear();
        this.server = null;
    }
}
