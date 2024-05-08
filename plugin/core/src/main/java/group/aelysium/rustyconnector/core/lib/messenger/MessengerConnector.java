package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.toolkit.core.UserPass;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.core.lib.crypt.AESCryptor;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.Optional;

public abstract class MessengerConnector implements IMessengerConnector {
    protected final InetSocketAddress address;
    protected final UserPass userPass;
    protected IMessengerConnection connection;
    protected final AESCryptor cryptor;

    protected MessengerConnector(AESCryptor cryptor, InetSocketAddress address, UserPass userPass) {
        this.address = address;
        this.userPass = userPass;
        this.cryptor = cryptor;
    }

    /**
     * Get the {@link MessengerConnection} created from this {@link MessengerConnector}.
     * @return An {@link Optional} possibly containing a {@link MessengerConnection}.
     */
    public Optional<IMessengerConnection> connection() {
        if(this.connection == null) return Optional.empty();
        return Optional.of(this.connection);
    }

    /**
     * Connect to the remote resource.
     * @return A {@link MessengerConnection}.
     * @throws ConnectException If there was an issue connecting to the remote resource.
     */
    public abstract IMessengerConnection connect() throws ConnectException;
}
