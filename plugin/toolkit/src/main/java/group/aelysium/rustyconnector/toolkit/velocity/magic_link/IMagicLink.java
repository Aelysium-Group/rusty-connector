package group.aelysium.rustyconnector.toolkit.velocity.magic_link;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

import java.net.ConnectException;
import java.util.Optional;

public interface IMagicLink extends Service {
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

    /**
     * Fetches a Magic Link MCLoader Config based on a name.
     * `name` is considered to be the name of the file found in `magic_configs` on the Proxy, minus the file extension.
     * @param name The name to look for.
     */
    Optional<MagicLinkMCLoaderSettings> magicConfig(String name);

    record MagicLinkMCLoaderSettings(
            String family,
            int weight,
            int soft_cap,
            int hard_cap
    ) {};
}
