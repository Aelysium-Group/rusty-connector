package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.api.velocity.parties.IParty;
import group.aelysium.rustyconnector.api.velocity.parties.SwitchPower;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.rmi.ConnectException;
import java.util.Random;
import java.util.Vector;

public class Party implements IParty<PlayerServer> {
    private final Vector<Player> players;
    private final int maxSize;
    private Player leader;
    private WeakReference<PlayerServer> server;

    public Party(int maxSize, Player host, PlayerServer server) {
        this.players = new Vector<>(maxSize);
        this.maxSize = maxSize;
        this.leader = host;
        this.players.add(host);
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
        if(!this.players.contains(this.leader) || !this.leader.isActive()) {
            this.newRandomLeader();
            this.broadcast(Component.text("The old party leader is no-longer available! "+this.leader.getUsername()+" is the new leader!", NamedTextColor.YELLOW));
        }

        return this.leader;
    }

    public void setLeader(Player player) {
        if(!this.players.contains(player))
            throw new IllegalStateException(player.getUsername() + " isn't in this party, they can't be made leader!");
        this.leader = player;
    }

    public void newRandomLeader() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        try {
            this.leader = this.players.get(new Random().nextInt(this.players.size()));
        } catch (Exception ignore) {
            this.leader = this.players.firstElement();
        }
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

        player.sendMessage(VelocityLang.PARTY_JOINED_SELF);
        this.players.forEach(partyMember -> partyMember.sendMessage(VelocityLang.PARTY_JOINED.build(partyMember.getUsername())));
        this.players.add(player);
    }

    public synchronized void leave(Player player) {
        if(this.isEmpty()) return;
        this.players.remove(player);

        if(this.isEmpty()) { // This was the last member of the party
            Tinder.get().services().party().orElseThrow().disband(this);
            return;
        }

        this.players.forEach(partyMember -> partyMember.sendMessage(Component.text(player.getUsername() + " left the party.", NamedTextColor.YELLOW)));

        if(player.equals(this.leader)) {
            newRandomLeader();
            this.broadcast(Component.text(this.leader.getUsername()+" is the new party leader!", NamedTextColor.YELLOW));
        }

        if(this.isEmpty())
            Tinder.get().services().party().orElseThrow().disband(this);
    }

    public void broadcast(Component message) {
        this.players.forEach(player -> player.sendMessage(message));
    }

    public boolean contains(Player player) {
        return this.players.contains(player);
    }

    public void decompose() {
        this.players.clear();
        this.leader = null;
    }

    public synchronized void connect(PlayerServer server) {
        SwitchPower switchPower = Tinder.get().services().party().orElseThrow().settings().switchPower();
        this.setServer(server);
        Vector<Player> kickedPlayers = new Vector<>();

        Tinder.get().services().party().orElseThrow().queueConnector(() -> {
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
                player.sendMessage(VelocityLang.PARTY_FOLLOWING_KICKED);
                this.leave(player);
            });
        });
    }

    @Override
    public String toString() {
        try {
            return "<Party players=" + this.players.size() + " leader=" + this.leader.getUsername() + ">";
        } catch (Exception ignore) {
            return "<Party players=" + this.players.size() + " leader=null>";
        }
    }
}
