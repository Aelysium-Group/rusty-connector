package group.aelysium.rustyconnector.core.lib.generic;

public interface Callable<K> {
    /**
     * Execute the callable
     */
    K execute();
}
