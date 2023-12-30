package group.aelysium.rustyconnector.toolkit.core.messenger;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.net.ConnectException;
import java.util.Optional;

public interface IMessengerConnector extends Service {
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
