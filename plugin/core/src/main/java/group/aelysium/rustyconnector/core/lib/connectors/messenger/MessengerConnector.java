package group.aelysium.rustyconnector.core.lib.connectors.messenger;

import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Optional;

public abstract class MessengerConnector<C extends MessengerConnection> extends Connector<C> {
    protected C connection;

    protected MessengerConnector(InetSocketAddress address, UserPass userPass) {
        super(address, userPass);
    }

    /**
     * Get the {@link MessengerConnection} created from this {@link MessengerConnector}.
     * @return An {@link Optional} possibly containing a {@link MessengerConnection}.
     */
    public Optional<C> connection() {
        if(this.connection == null) return Optional.empty();
        return Optional.of(this.connection);
    }

    /**
     * Connect to the remote resource.
     * @return A {@link MessengerConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    public abstract C connect() throws ConnectException;

    @Override
    public void kill() {
        if(this.connection != null) this.connection.kill();
    }
}
