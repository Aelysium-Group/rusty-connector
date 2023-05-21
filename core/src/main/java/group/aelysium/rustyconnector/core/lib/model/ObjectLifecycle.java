package group.aelysium.rustyconnector.core.lib.model;

public interface ObjectLifecycle {
    /**
     * Init the object.
     */
    void init();

    /**
     * Should reload the object.
     * If the object has state generated as a result of a config file, this should completely reload based on the config file.
     */
    void reload();

    /**
     * Kill the object. Once this has been called the object should be guaranteed to be garbage collected.
     */
    void kill();
}
