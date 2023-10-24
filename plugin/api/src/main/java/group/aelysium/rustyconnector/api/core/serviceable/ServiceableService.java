package group.aelysium.rustyconnector.api.core.serviceable;

import group.aelysium.rustyconnector.api.core.serviceable.interfaces.ServiceHandler;

public abstract class ServiceableService<H extends ServiceHandler> {
    protected H services;

    public ServiceableService(H services) {
        this.services = services;
    }

    public H services() {
        return this.services;
    }

    public void kill() {
        this.services.killAll();
    }
}
