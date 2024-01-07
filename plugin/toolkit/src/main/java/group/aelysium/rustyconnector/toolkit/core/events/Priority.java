package group.aelysium.rustyconnector.toolkit.core.events;

public class Priority {
    /**
     * Runs before RustyConnector's native event listeners run.
     */
    public static final int VERY_FIRST = Integer.MIN_VALUE;

    /**
     * Runs before RustyConnector's native event listeners run.
     */
    public static final int BEFORE_NATIVE = -1;

    /**
     * The priority level used by RustyConnector's own event listeners.
     */
    public static final int NATIVE = 0;

    /**
     * Runs after RustyConnector's native event listeners run.
     */
    public static final int AFTER_NATIVE = 1;

    /**
     * Runs after RustyConnector's native event listeners run.
     */
    public static final int VERY_LAST = Integer.MAX_VALUE;
}
