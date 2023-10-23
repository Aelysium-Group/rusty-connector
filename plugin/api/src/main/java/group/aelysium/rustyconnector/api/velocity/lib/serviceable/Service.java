package group.aelysium.rustyconnector.api.velocity.lib.serviceable;

public interface Service {
    /**
     * Kill the service.
     * Every service is solely responsible for killing it's own processes.
     */
    void kill();
}
