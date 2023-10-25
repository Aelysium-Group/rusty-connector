package group.aelysium.rustyconnector.api.core.messenger;

import group.aelysium.rustyconnector.api.core.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.api.core.message_cache.IMessageCacheService;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.net.ConnectException;
import java.util.Optional;

public interface IMessengerConnector<TMessengerConnection extends IMessengerConnection<? extends IMessageCacheService<? extends ICacheableMessage>>> extends Service {
    /**
     * Gets the connection to the remote resource.
     * @return {@link IMessengerConnection}
     */
    Optional<TMessengerConnection> connection();

    /**
     * Connect to the remote resource.
     *
     * @return A {@link IMessengerConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    TMessengerConnection connect() throws ConnectException;
}
