package group.aelysium.rustyconnector.toolkit.mc_loader.magic_link;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.net.ConnectException;
import java.util.Optional;

public interface IMagicLinkService extends Service {
    /**
     * Sets the status of this server's magic link.
     * Depending on the status this server may make requests to the proxy in different ways.
     * @param status The status to be set.
     */
    void setStatus(MagicLinkStatus status);

    /**
     * Set the ping delay for this upcoming ping.
     * @param delay The delay to set.
     */
    void setUpcomingPingDelay(int delay);

    /**
     * Starts the heartbeat that this server's magic link uses.
     */
    void startHeartbeat();

    /**
     * Gets the connection to the remote resource.
     * @return {@link IMessengerConnection}
     */
    Optional<IMessengerConnection> connection();

    /**
     * Connect to the remote resource.
     *
     * @return A {@link IMessengerConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    IMessengerConnection connect() throws ConnectException;
}
