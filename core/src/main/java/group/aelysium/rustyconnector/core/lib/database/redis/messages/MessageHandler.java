package group.aelysium.rustyconnector.core.lib.database.redis.messages;

import java.security.InvalidAlgorithmParameterException;

public interface MessageHandler {
    /**
     * Execute the defined processor.
     * @throws InvalidAlgorithmParameterException If there is an issue processing the message.
     */
    void execute() throws Exception;
}
