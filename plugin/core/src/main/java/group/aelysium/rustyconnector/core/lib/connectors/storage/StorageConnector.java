package group.aelysium.rustyconnector.core.lib.connectors.storage;

import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Optional;

public abstract class StorageConnector<C extends StorageConnection> extends Connector<C> {
    protected C connection;
    protected StorageConnector(InetSocketAddress address, UserPass userPass) {
        super(address, userPass);
    }

    /**
     * Get the {@link StorageConnection} created from this {@link StorageConnector}.
     * @return An {@link Optional} possibly containing a {@link StorageConnection}.
     */
    public Optional<C> connection() {
        if(this.connection == null) return Optional.empty();
        return Optional.of(this.connection);
    }

    /**
     * Connect to the remote resource.
     * @return A {@link StorageConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    public abstract C connect() throws ConnectException;

    @Override
    public void kill() {
        if(this.connection != null) this.connection.kill();
    }
}
