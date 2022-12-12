package group.aelysium.rustyconnector.core.lib.data_messaging;

import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;

import java.security.InvalidAlgorithmParameterException;

public interface MessageHandler {
    /**
     * Execute the defined processor.
     * @throws InvalidAlgorithmParameterException If there is an issue processing the message.
     */
    void execute() throws Exception;
}
