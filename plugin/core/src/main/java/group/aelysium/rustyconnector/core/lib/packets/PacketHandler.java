package group.aelysium.rustyconnector.core.lib.packets;

import java.security.InvalidAlgorithmParameterException;

public interface PacketHandler {
    /**
     * Execute the defined processor.
     * @throws InvalidAlgorithmParameterException If there is an issue processing the message.
     */
    void execute() throws Exception;
}
