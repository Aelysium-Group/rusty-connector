package group.aelysium.rustyconnector.toolkit.core.serviceable;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceHandler;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceableService;

public abstract class ServiceableService<TServiceHandler extends IServiceHandler> implements IServiceableService<TServiceHandler> {
    protected TServiceHandler services;

    public ServiceableService(TServiceHandler services) {
        this.services = services;
    }

    public TServiceHandler services() {
        return this.services;
    }

    public void kill() {
        this.services.killAll();
    }
}
