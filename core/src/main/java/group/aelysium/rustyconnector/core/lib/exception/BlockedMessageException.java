package group.aelysium.rustyconnector.core.lib.exception;

/**
 * An exception that should never result in output being sent to the client.
 */
public class BlockedMessageException
        extends RuntimeException {
    public BlockedMessageException(String message) {
        super(message);
    }
}