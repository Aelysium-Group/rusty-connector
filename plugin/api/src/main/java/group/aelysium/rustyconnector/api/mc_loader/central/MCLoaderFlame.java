package group.aelysium.rustyconnector.api.mc_loader.central;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceableService;

public abstract class MCLoaderFlame<S extends ServiceHandler> extends ServiceableService<S> {

    public MCLoaderFlame(S services) {
        super(services);
    }

    public abstract String versionAsString();

    public abstract void exhaust();

    public abstract MessengerConnector<? extends MessengerConnection> backbone();
}