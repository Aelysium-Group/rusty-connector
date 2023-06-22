package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class Party {
    private boolean empty = false;
    private final Vector<Player> players;
    private final int size;
    private Player host;
    private WeakReference<PlayerServer> server;

    public Party(int size, Player host) {
        this.players = new Vector<>(size);
        this.size = size;
        this.setHost(host);
    }

    public void setServer(PlayerServer server) {
        this.server = new WeakReference<>(server);
    }
    public PlayerServer getServer() {
        return this.server.get();
    }

    public Player getHost() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        return this.host;
    }

    private void setHost(Player player) {
        this.host = player;
        this.players.add(player);
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public void join(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        if(this.players.size() > this.size) throw new RuntimeException("The party is already full! Try again later!");

        this.players.add(player);
    }

    public void leave(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        this.players.remove(player);

        if(this.players.size() <= 0)
            this.decompose();
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    private void decompose() {
        this.empty = true;
        this.players.clear();
        this.host = null;
    }
}
