package group.aelysium.rustyconnector.core.lib.serviceable;

public abstract class ServiceableService<H extends ServiceHandler> extends Service {
    protected H services;

    public ServiceableService(H services) {
        this.services = services;
    }

    public H services() {
        return this.services;
    }

    @Override
    public void kill() {
        this.services.killAll();
    }
}
