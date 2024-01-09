package group.aelysium.rustyconnector.toolkit.mc_loader.magic_link;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;

import java.net.ConnectException;
import java.util.Optional;

public interface IMagicLinkService extends Service {
    /**
     * Set the ping delay for this upcoming ping.
     * @param delay The delay to set.
     */
    void setDelay(int delay);

    /**
     * Starts the heartbeat that this server's magic link uses.
     */
    void startHeartbeat(IMCLoaderFlame<? extends ICoreServiceHandler> api);

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
