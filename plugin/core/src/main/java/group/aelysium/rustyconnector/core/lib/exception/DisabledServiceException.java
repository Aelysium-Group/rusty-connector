package group.aelysium.rustyconnector.core.lib.exception;

/**
 * An exception that should never result in output being sent to the client.
 */
public class DisabledServiceException
        extends RuntimeException {
    public DisabledServiceException(String message) {
        super(message);
    }
}