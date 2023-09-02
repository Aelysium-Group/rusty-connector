package group.aelysium.rustyconnector.core.lib.connectors.messenger;

import group.aelysium.rustyconnector.core.lib.connectors.Connection;
import group.aelysium.rustyconnector.core.lib.database.redis.messages.GenericRedisMessage;

public abstract class MessengerConnection extends Connection {
    /**
     * Start listening on the messenger connection for messages.
     * @param subscriber A class instance of the listener to be used.
     * @throws IllegalStateException If the service is already running.
     */
    public abstract void startListening(Class<? extends MessengerSubscriber> subscriber);

    /**
     * Publish a new message to the {@link MessengerConnection}.
     * @param message The message to publish.
     */
    public abstract void publish(GenericRedisMessage message);

    /**
     * Validate a private key.
     * @param privateKey The private key that needs to be validated.
     * @return `true` if the key is valid. `false` otherwise.
     */
    public abstract boolean validatePrivateKey(char[] privateKey);
}
