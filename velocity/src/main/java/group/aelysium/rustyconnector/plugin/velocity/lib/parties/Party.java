package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Vector;

public class Party {
    private boolean empty = false;
    private final Vector<Player> players;
    private final int maxSize;
    private WeakReference<Player> leader;
    private WeakReference<PlayerServer> server;

    public Party(int maxSize, Player host) {
        this.players = new Vector<>(maxSize);
        this.maxSize = maxSize;
        this.setLeader(host);
    }

    public void setServer(PlayerServer server) {
        this.server = new WeakReference<>(server);
    }
    public PlayerServer getServer() {
        return this.server.get();
    }

    public Player getLeader() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        if(this.leader.get() == null) {
            Player newLeader = players.get(0);
            this.setLeader(newLeader);
            this.broadcast(Component.text("The old party leader is no-longer available! "+newLeader.getUsername()+" is the new leader!", NamedTextColor.YELLOW));
        }

        return this.leader.get();
    }

    private void setLeader(Player player) {
        this.leader = new WeakReference<>(player);
    }

    public boolean isEmpty() {
        return this.empty;
    }

    public Vector<Player> players() {
        return this.players;
    }

    public void join(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        if(this.players.size() > this.maxSize) throw new RuntimeException("The party is already full! Try again later!");

        this.getServer().connect(player);
        this.getServer().playerJoined();

        this.players.forEach(partyMember -> player.sendMessage(Component.text(player.getUsername() + " joined the party.", NamedTextColor.YELLOW)));
        this.players.add(player);
    }

    public void leave(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        this.players.remove(player);

        this.players.forEach(partyMember -> player.sendMessage(Component.text(player.getUsername() + " left the party.", NamedTextColor.YELLOW)));

        if(this.players.size() == 0)
            this.decompose();
    }

    public void broadcast(Component message) {
        this.players.forEach(player -> player.sendMessage(message));
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    public void decompose() {
        this.empty = true;
        this.players.clear();
        this.leader.clear();
        this.leader = null;
    }
}
