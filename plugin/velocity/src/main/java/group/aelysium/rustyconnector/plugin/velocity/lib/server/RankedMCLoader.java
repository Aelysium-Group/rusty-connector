package group.aelysium.rustyconnector.plugin.velocity.lib.server;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.toolkit.core.packet.GenericPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketIdentification;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.gameplay.ISession;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.storage.IRankedPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IRankedMCLoader;

import java.net.InetSocketAddress;
import java.rmi.ConnectException;
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
        List<Player> kickedPlayers = new Vector<>();
        ProxyServer velocityServer = Tinder.get().velocityServer();

        for (IRankedPlayer rankedPlayer : session.players()) {
            try {
                com.velocitypowered.api.proxy.Player player = velocityServer.getPlayer(rankedPlayer.uuid()).orElseThrow();
                try {
                    this.directConnect(player);
                } catch (ConnectException e) {
                    kickedPlayers.add(player);
                }
            } catch (NoSuchElementException ignore) {
            } // Player isn't online, so it's not like we could message them anyway.
        }

        kickedPlayers.forEach(player -> {
            // player.sendMessage(VelocityLang.GAME_FOLLOW_KICKED);
        });

        GenericPacket packet = new GenericPacket.MCLoaderPacketBuilder()
                .identification(PacketIdentification.Predefined.START_RANKED_GAME)
                .sendingToAnotherMCLoader(this.uuid())
                .parameter("session", session.toJSON().toString())
                .build();
        Tinder.get().services().magicLink().connection().orElseThrow().publish(packet);

        this.activeSession = session;
        this.lock();
    }

    public void unlock() {
        this.activeSession = null;
        super.unlock();
    }
}
