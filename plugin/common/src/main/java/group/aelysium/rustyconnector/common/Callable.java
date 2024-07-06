package group.aelysium.rustyconnector.common;

public interface Callable<K> {
    /**
     * Execute the callable
     */
    K execute();
}
