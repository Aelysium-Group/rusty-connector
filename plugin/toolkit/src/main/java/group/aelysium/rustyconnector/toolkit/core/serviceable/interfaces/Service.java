package group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces;

public interface Service {
    /**
     * Kill the service.
     * Every service is solely responsible for killing its own processes.
     */
    void kill();
}
