package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import group.aelysium.rustyconnector.toolkit.velocity.parties.SwitchPower;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.rmi.ConnectException;
import java.util.Random;
import java.util.Vector;

public class Party implements group.aelysium.rustyconnector.toolkit.velocity.parties.IParty {
    private final Vector<IPlayer> players;
    private final int maxSize;
    private IPlayer leader;
    private WeakReference<IMCLoader> server;

    public Party(int maxSize, IPlayer host, IMCLoader server) {
        this.players = new Vector<>(maxSize);
        this.maxSize = maxSize;
        this.leader = host;
        this.players.add(host);
        this.server = new WeakReference<>(server);
    }

    public void setServer(IMCLoader server) {
        if(server.equals(server())) return;

        this.server = new WeakReference<>(server);
    }
    public IMCLoader server() {
        return this.server.get();
    }

    public synchronized IPlayer leader() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        if(!this.players.contains(this.leader) || this.leader.online()) {
            this.newRandomLeader();
            this.broadcast(Component.text("The old party leader is no-longer available! "+this.leader.username()+" is the new leader!", NamedTextColor.YELLOW));
        }

        return this.leader;
    }

    public void setLeader(IPlayer player) {
        if(!this.players.contains(player))
            throw new IllegalStateException(player.username() + " isn't in this party, they can't be made leader!");
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

    public Vector<IPlayer> players() {
        return this.players;
    }

    public synchronized void join(IPlayer player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        if(this.players.size() > this.maxSize) throw new RuntimeException("The party is already full! Try again later!");

        player.sendMessage(ProxyLang.PARTY_JOINED_SELF);
        this.players.forEach(partyMember -> partyMember.sendMessage(ProxyLang.PARTY_JOINED.build(partyMember.username())));
        this.players.add(player);
    }

    public synchronized void leave(IPlayer player) {
        if(this.isEmpty()) return;
        this.players.remove(player);

        if(this.isEmpty()) { // This was the last member of the party
            Tinder.get().services().party().orElseThrow().disband(this);
            return;
        }

        this.players.forEach(partyMember -> partyMember.sendMessage(Component.text(player.username() + " left the party.", NamedTextColor.YELLOW)));

        if(player.equals(this.leader)) {
            newRandomLeader();
            this.broadcast(Component.text(this.leader.username()+" is the new party leader!", NamedTextColor.YELLOW));
        }

        if(this.isEmpty())
            Tinder.get().services().party().orElseThrow().disband(this);
    }

    public void broadcast(Component message) {
        this.players.forEach(player -> player.sendMessage(message));
    }

    public boolean contains(IPlayer player) {
        return this.players.contains(player);
    }

    public void decompose() {
        this.players.clear();
        this.leader = null;
    }

    public synchronized void connect(IMCLoader server) {
        SwitchPower switchPower = Tinder.get().services().party().orElseThrow().settings().switchPower();
        boolean kickOnSendFailure = Tinder.get().services().party().orElseThrow().settings().kickOnSendFailure();

        this.setServer(server);

        Tinder.get().services().party().orElseThrow().queueConnector(() -> {
            for (IPlayer player : this.players)
                try {
                    switch (switchPower) {
                        case MINIMAL -> {
                            if(server.full()) {
                                if (kickOnSendFailure) {
                                    player.sendMessage(VelocityLang.PARTY_FOLLOWING_KICKED_SERVER_FULL);
                                    this.leave(player);
                                } else player.sendMessage(VelocityLang.PARTY_FOLLOWING_FAILED_SERVER_FULL);
                                return;
                            }
                            server.directConnect(player);
                        }
                        case MODERATE -> {
                            if(server.maxed()) {
                                if (kickOnSendFailure) {
                                    player.sendMessage(VelocityLang.PARTY_FOLLOWING_KICKED_SERVER_FULL);
                                    this.leave(player);
                                } else player.sendMessage(VelocityLang.PARTY_FOLLOWING_FAILED_SERVER_FULL);
                                return;
                            }
                            server.directConnect(player);
                        }
                        case AGGRESSIVE -> server.directConnect(player);
                    }
                } catch (ConnectException e) {
                    if (kickOnSendFailure) {
                        player.sendMessage(VelocityLang.PARTY_FOLLOWING_KICKED_GENERIC);
                        this.leave(player);
                    } else player.sendMessage(VelocityLang.PARTY_FOLLOWING_FAILED_GENERIC);
                }
        });
    }

    @Override
    public String toString() {
        try {
            return "<Party players=" + this.players.size() + " leader=" + this.leader.username() + ">";
        } catch (Exception ignore) {
            return "<Party players=" + this.players.size() + " leader=null>";
        }
    }
}
