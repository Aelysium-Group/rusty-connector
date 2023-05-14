package group.aelysium.rustyconnector.plugin.velocity.lib.family.rounded;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RoundedServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;
import net.kyori.adventure.text.Component;

import java.util.Collection;

public class RoundedServer extends PlayerServer {
    private RoundedSession session;

    public RoundedServer(ServerInfo serverInfo, int softPlayerCap, int hardPlayerCap, int weight) {
        super(serverInfo, softPlayerCap, hardPlayerCap, weight);
        this.session = NullRoundedSession.get();
    }

    public RoundedSession getSession() {
        return this.session;
    }

    /**
     * Start a new session with the provided round.
     * @param round The round to start a session with.
     */
    public void newSession(RoundedSessionGroup round) {
        if(!(this.session instanceof NullRoundedSession)) throw new IllegalStateException("You can't start a new session while one is active!");
        this.session = new RoundedSession(round);
    }

    /**
     * End the current session.
     * This method will kick all current players out into the parent family.
     *
     * Additionally, this server will inform its family that it is now open for another session to join.
     */
    public void endSession() {
        if(this.session instanceof NullRoundedSession) return;
        this.session = NullRoundedSession.get();

        RoundedServerFamily family = (RoundedServerFamily) this.getFamily();

        Collection<Player> players = this.getRegisteredServer().getPlayersConnected();

        for (Player player: players) {
            try {
                family.getParentFamily().connect(player);
            } catch (Exception e) {
                player.disconnect(Component.text(e.getMessage()));
            }
        }

        ((RoundedServerFamily) this.getFamily()).startSessions();
    }


    /**
     * Validates the player against the server's current player count.
     * If the server is full or the player doesn't have permissions to bypass soft and hard player caps. They will be kicked
     *
     * @param player The player to validate
     * @return `true` if the player is able to join. `false` otherwise.
     * @deprecated This method always returns `false`. Running validation checks against players directly isn't supported for RoundedServers.
     */
    @Deprecated
    @Override
    public boolean validatePlayer(Player player) {
        return false;
    }

    /**
     * Connects a round of players to the server.
     * This also increases the player count on this server by 1.
     * @param round The round of players to connect.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     */
    public boolean connect(RoundedSessionGroup round) {
        try {
            round.forEach(player -> {
                ConnectionRequestBuilder connection = player.createConnectionRequest(this.getRegisteredServer());
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
     * Connects a player to the server.
     * This also increases the player count on this server by 1.
     * @param player The player to connect.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     * @deprecated Use {@link #connect(RoundedSessionGroup)} instead.
     */
    @Deprecated
    @Override
    public boolean connect(Player player) {
        throw new RuntimeException("You can't connect specific players to a RoundedServer!");
    }

    /**
     * Set's a connections initial server to the server.
     * @param event The connection to set.
     * @return `true` if the connection succeeds. `false` if the connection encounters an exception.
     * @deprecated Use {@link #connect(RoundedSessionGroup)} instead.
     */
    @Deprecated
    @Override
    public boolean connect(PlayerChooseInitialServerEvent event) {
        throw new RuntimeException("You can't connect specific players to a RoundedServer!");
    }
}
