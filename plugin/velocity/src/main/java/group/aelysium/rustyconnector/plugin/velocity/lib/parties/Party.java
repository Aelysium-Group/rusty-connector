package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.rmi.ConnectException;
import java.util.Objects;
import java.util.Vector;

public class Party {
    private final Vector<Player> players;
    private final int maxSize;
    private WeakReference<Player> leader;
    private WeakReference<PlayerServer> server;

    public Party(int maxSize, Player host, PlayerServer server) {
        this.players = new Vector<>(maxSize);
        this.maxSize = maxSize;
        this.setLeader(host);
        this.server = new WeakReference<>(server);
    }

    public void setServer(PlayerServer server) {
        if(server.equals(server())) return;

        this.server = new WeakReference<>(server);
    }
    public PlayerServer server() {
        return this.server.get();
    }

    public synchronized Player leader() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        if(this.leader.get() == null) {
            Player newLeader = players.lastElement();
            this.setLeader(newLeader);
            this.broadcast(Component.text("The old party leader is no-longer available! "+newLeader.getUsername()+" is the new leader!", NamedTextColor.YELLOW));
        }

        return this.leader.get();
    }

    /**
     * Assign a new leader to the party.
     * If `null` is passed, this will find a random player and make them leader.
     * @param player The player to make leader. Or `null` to assign a random member.
     */
    public void setLeader(Player player) {
        if(player == null) {
            this.leader.clear();
            leader();
        }

        if(!this.players.contains(player))
            this.players.add(player);
        this.leader = new WeakReference<>(player);
    }

    public boolean isEmpty() {
        return this.players().isEmpty();
    }

    public Vector<Player> players() {
        return this.players;
    }

    public synchronized void join(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        if(this.players.size() > this.maxSize) throw new RuntimeException("The party is already full! Try again later!");

        player.sendMessage(Component.text("You joined the party!", NamedTextColor.GREEN));
        this.players.forEach(partyMember -> partyMember.sendMessage(Component.text(player.getUsername() + " joined the party.", NamedTextColor.YELLOW)));
        this.players.add(player);
    }

    public synchronized void leave(Player player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        this.players.remove(player);

        this.players.forEach(partyMember -> partyMember.sendMessage(Component.text(player.getUsername() + " left the party.", NamedTextColor.YELLOW)));

        if(player.equals(leader())) setLeader(null);

        if(this.isEmpty())
            VelocityAPI.get().services().partyService().orElseThrow().disband(this);
    }

    public void broadcast(Component message) {
        this.players.forEach(player -> player.sendMessage(message));
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    public void decompose() {
        this.players.clear();
        this.leader.clear();
        this.leader = null;
    }

    public synchronized void connect(PlayerServer server) {
        SwitchPower switchPower = VelocityAPI.get().services().partyService().orElseThrow().settings().switchPower();
        this.setServer(server);
        Vector<Player> kickedPlayers = new Vector<>();

        VelocityAPI.get().services().partyService().orElseThrow().queueConnector(() -> {
            for (Player player : this.players)
                try {
                    switch (switchPower) {
                        case MINIMAL -> {
                            if(server.full()) {
                                kickedPlayers.add(player);
                                return;
                            }
                            server.directConnect(player);
                        }
                        case MODERATE -> {
                            if(server.maxed()) {
                                kickedPlayers.add(player);
                                return;
                            }
                            server.directConnect(player);
                        }
                        case AGGRESSIVE -> server.directConnect(player);
                    }
                } catch (ConnectException e) {
                    kickedPlayers.add(player);
                }

            kickedPlayers.forEach(player -> {
                player.sendMessage(Component.text("There was an issue following your party! You've been kicked.", NamedTextColor.RED));
                this.leave(player);
            });
        });
    }

    @Override
    public String toString() {
        try {
            return "<Party players=" + this.players.size() + " leader=" + Objects.requireNonNull(this.leader.get()).getUsername() + ">";
        } catch (Exception ignore) {
            return "<Party players=" + this.players.size() + " leader=null>";
        }
    }
}
