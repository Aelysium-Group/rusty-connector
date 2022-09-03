package group.aelysium.rustyconnector.core.generic.lib.database;

import java.security.InvalidAlgorithmParameterException;

public interface MessageProcessor {

    /**
     * Execute the defined processor.
     * @param message The `RedisMessage` to process
     * @throws InvalidAlgorithmParameterException If there is an issue processing the message.
     */
    void execute(RedisMessage message) throws InvalidAlgorithmParameterException;
}
