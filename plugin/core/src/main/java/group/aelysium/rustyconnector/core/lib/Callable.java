package group.aelysium.rustyconnector.core.lib;

public interface Callable<K> {
    /**
     * Execute the callable
     */
    K execute();
}
