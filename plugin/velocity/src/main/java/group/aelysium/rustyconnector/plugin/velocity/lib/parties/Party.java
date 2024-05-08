package group.aelysium.rustyconnector.plugin.velocity.lib.parties;

import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.parties.SwitchPower;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.ProxyLang;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Party implements group.aelysium.rustyconnector.toolkit.velocity.parties.IParty {
    private final Map<UUID, IPlayer> players;
    private final int maxSize;
    private UUID leader;
    private WeakReference<IMCLoader> server;

    public Party(int maxSize, IPlayer host, IMCLoader server) {
        this.players = new ConcurrentHashMap<>(maxSize);
        this.maxSize = maxSize;
        this.players.put(host.uuid(), host);
        this.leader = host.uuid();
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
        if(this.players.get(this.leader) == null) {
            IPlayer player = this.randomPlayer();
            this.leader = player.uuid();
            this.broadcast(Component.text("The old party leader is no-longer available! "+player.username()+" is the new leader!", NamedTextColor.YELLOW));
        }

        return this.players.get(this.leader);
    }

    public void setLeader(IPlayer player) {
        if(!this.players.containsKey(player.uuid())) throw new IllegalStateException();
        this.leader = player.uuid();
    }

    public IPlayer randomPlayer() {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");
        try {
            return this.players.values().stream().findAny().orElseThrow();
        } catch (Exception ignore) {
            return this.players.values().stream().findFirst().orElseThrow();
        }
    }

    public boolean isEmpty() {
        return this.players().isEmpty();
    }

    public List<IPlayer> players() {
        return this.players.values().stream().toList();
    }

    public synchronized void join(IPlayer player) {
        if(this.isEmpty()) throw new IllegalStateException("This party is empty and is no-longer useable!");

        if(this.players.size() > this.maxSize) throw new RuntimeException("The party is already full! Try again later!");

        player.sendMessage(ProxyLang.PARTY_JOINED_SELF);
        this.players.values().forEach(partyMember -> partyMember.sendMessage(ProxyLang.PARTY_JOINED.build(player.username())));
        this.players.put(player.uuid(), player);
    }

    public synchronized void leave(IPlayer player) {
        if(this.isEmpty()) return;
        this.players.remove(player.uuid());

        if(this.isEmpty()) { // This was the last member of the party
            Tinder.get().services().party().orElseThrow().disband(this);
            return;
        }

        this.players.values().forEach(partyMember -> partyMember.sendMessage(Component.text(player.username() + " left the party.", NamedTextColor.YELLOW)));

        if(player.uuid().equals(this.leader)) {
            IPlayer newLeader = randomPlayer();
            setLeader(newLeader);
            this.broadcast(Component.text(newLeader.username()+" is the new party leader!", NamedTextColor.YELLOW));
        }

        if(this.isEmpty())
            Tinder.get().services().party().orElseThrow().disband(this);
    }

    public void broadcast(Component message) {
        this.players.values().forEach(player -> player.sendMessage(message));
    }

    public boolean contains(IPlayer player) {
        return this.players.containsKey(player.uuid());
    }

    public void decompose() {
        this.players.clear();
        this.leader = null;
    }

    @Override
    public String toString() {
        try {
            return "<Party players=" + this.players.size() + " leader=" + this.players.get(this.leader) + ">";
        } catch (Exception ignore) {
            return "<Party players=" + this.players.size() + " leader=null>";
        }
    }

    /**
     * Checks is the player is a member of a party.
     * @param player The player.
     * @return A party if the player is in one. Otherwise, returns an empty optional.
     */
    public static Optional<IParty> locate(IPlayer player) {
        try {
            return Tinder.get().services().party().orElseThrow().find(player);
        } catch (Exception ignore) {}
        return Optional.empty();
    }

    public static boolean allowedToInitiateConnection(IParty party, IPlayer player) {
        try {
            if(!party.contains(player)) return false;

            if(party.leader().equals(player)) return true;

            return !Tinder.get().services().party().orElseThrow().settings().onlyLeaderCanInvite();
        } catch (Exception ignore) {}
        return true;
    }
}
