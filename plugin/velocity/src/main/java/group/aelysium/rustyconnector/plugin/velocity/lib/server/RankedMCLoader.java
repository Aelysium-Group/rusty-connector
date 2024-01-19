package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import group.aelysium.rustyconnector.core.lib.packets.BuiltInIdentifications;
import group.aelysium.rustyconnector.core.lib.packets.RankedGame;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.net.InetSocketAddress;
import java.util.*;

public class RankedMCLoader extends MCLoader implements IRankedMCLoader {
    protected ISession activeSession;

    public RankedMCLoader(UUID uuid, InetSocketAddress address, String displayName, int softPlayerCap, int hardPlayerCap, int weight, int timeout) {
        super(uuid, address, displayName, softPlayerCap, hardPlayerCap, weight, timeout);
    }

    public Optional<ISession> currentSession() {
        if(this.activeSession == null) return Optional.empty();
        return Optional.of(this.activeSession);
    }

    public void connect(ISession session) {
        for (IPlayer rankedPlayer : session.players())
            this.connect(rankedPlayer);

        Packet packet = Tinder.get().services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_READY)
                .sendingToMCLoader(this.uuid())
                .parameter(RankedGame.Ready.Parameters.SESSION, session.toJSON().toString())
                .build();
        Tinder.get().services().magicLink().connection().orElseThrow().publish(packet);

        this.activeSession = session;
        this.lock();
    }

    @Override
    public void leave(IPlayer player) {
        if(this.activeSession == null) return;

        if(!this.activeSession.players().remove(player)) return;

        ISession.Settings settings = this.activeSession.settings();

        if(this.activeSession.players().size() >= settings.min()) return;


        this.implodeSession("To many people left the server! There aren't enough players to meet the minimum player requirements.");
    }

    public void implodeSession(String reason) {
        Packet packet = Tinder.get().services().packetBuilder().newBuilder()
                .identification(BuiltInIdentifications.RANKED_GAME_IMPLODE)
                .sendingToMCLoader(this.uuid())
                .parameter(RankedGame.Imploded.Parameters.REASON, reason)
                .parameter(RankedGame.Imploded.Parameters.SESSION_UUID, this.activeSession.uuid().toString())
                .build();
        Tinder.get().services().magicLink().connection().orElseThrow().publish(packet);
        this.activeSession.implode();
        this.unlock();
    }

    public void unlock() {
        this.activeSession = null;
        super.unlock();
    }
}
