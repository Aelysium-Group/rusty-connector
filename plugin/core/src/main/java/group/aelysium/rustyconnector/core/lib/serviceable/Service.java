package group.aelysium.rustyconnector.core.lib.serviceable;

public abstract class Service {
    /**
     * Kill the service.
     * Every service is solely responsible for killing it's own processes.
     */
    public abstract void kill();
}
