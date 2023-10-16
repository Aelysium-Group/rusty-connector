package group.aelysium.rustyconnector.core.lib.connectors;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.sql.SQLException;

public abstract class Connector<C extends Connection> extends Service {
    protected final InetSocketAddress address;
    protected final UserPass userPass;

    protected Connector(InetSocketAddress address, UserPass userPass) {
        this.address = address;
        this.userPass = userPass;
    }

    /**
     * Connect to the remote resource.
     * @return A {@link Connection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    public abstract C connect() throws ConnectException, SQLException;
}
