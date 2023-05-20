package group.aelysium.rustyconnector.plugin.velocity.lib.family;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.PlayerFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.SystemFocusedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedServerManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedSession;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedSessionManager;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded.RoundedServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.processor.VirtualProxyProcessor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class RoundedServerFamily extends SystemFocusedServerFamily<RoundedServer> {
    private final RoundedServerManager servers = new RoundedServerManager();
    private final RoundedSessionManager roundedSessionManager;
    private final PlayerFocusedServerFamily parentFamily;

    private RoundedServerFamily(String name, PlayerFocusedServerFamily parentFamily, int minRoundPlayers, int maxRoundPlayers) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        super(name);

        this.parentFamily = parentFamily;
        this.roundedSessionManager = new RoundedSessionManager(minRoundPlayers, maxRoundPlayers);
    }

    public RoundedSessionManager getSessionManager() {
        return this.roundedSessionManager;
    }

    public PlayerFocusedServerFamily getParentFamily() {
        return this.parentFamily;
    }

    public RoundedSessionManager getRoundManager() {
        return this.roundedSessionManager;
    }

    /**
     * Pre-connect the player to this family.
     * Once possible, the player will be connected to the family.
     */
    public void preConnect(Player player) {
        this.roundedSessionManager.join(player);
        this.roundedSessionManager.queueValidGroups();
    }

    /**
     * Cancel this player's pre-connection to the family.
     */
    public void cancelPreConnection(Player player) {
        this.roundedSessionManager.leave(player);
    }

    /**
     * Take any valid player rounds that are ready for sessions and start a session with them.
     * If no servers are available to have sessions opened, don't open any.
     */
    public void startAvailableSessions() {
        List<RoundedServer> servers = this.servers.findAvailable();
        if (servers.size() <= 0) return;
        if (this.roundedSessionManager.getReadyGroups() <= 0) return;

        for (RoundedServer server : servers) {
            RoundedSession session = this.roundedSessionManager.popSessionFromQueue();
            if (session == null) return;

            server.assignSession(session);

            server.startSession();
        }
    }

    /**
     * Initializes all server families based on the configs.
     * By the time this runs, the configuration file should be able to guarantee that all values are present.
     * @return A list of all server families.
     */
    public static RoundedServerFamily init(VirtualProxyProcessor virtualProxyProcessor, String familyName) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {        VelocityAPI api = VelocityRustyConnector.getAPI();
        return null;
    }

    @Override
    public long getPlayerCount() {
        AtomicLong newPlayerCount = new AtomicLong();
        this.servers.forEach(server -> newPlayerCount.addAndGet(server.getPlayerCount()));

        return newPlayerCount.get();
    }

    @Override
    public List<RoundedServer> getRegisteredServers() {
        return this.servers.dump();
    }

    @Override
    public void addServer(RoundedServer server) {
        this.servers.add(server);
    }

    @Override
    public void removeServer(RoundedServer server) {
        this.servers.remove(server);
    }

    @Override
    public RoundedServer getServer(@NotNull ServerInfo serverInfo) {
        return this.getRegisteredServers().stream()
                .filter(server -> Objects.equals(server.getServerInfo(), serverInfo)
                ).findFirst().orElse(null);
    }

    @Override
    public void unregisterServers() throws Exception {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        for (PlayerServer server : this.servers.dump()) {
            if(server == null) continue;
            api.getVirtualProcessor().unregisterServer(server.getServerInfo(),this.name, false);
        }
    }

    @Override
    public List<Player> getAllPlayers(int max) {
        List<Player> players = new ArrayList<>();

        for (PlayerServer server : this.getRegisteredServers()) {
            if(players.size() > max) break;

            players.addAll(server.getRegisteredServer().getPlayersConnected());
        }

        return players;
    }
}