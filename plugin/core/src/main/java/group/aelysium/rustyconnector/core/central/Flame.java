package group.aelysium.rustyconnector.core.central;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.core.plugin.central.CoreServiceHandler;

public abstract class Flame<S extends ServiceHandler> extends ServiceableService<S> {

    public Flame(S services) {
        super(services);
    }

    public abstract String versionAsString();

    public abstract void exhaust();

    public abstract MessengerConnector<? extends MessengerConnection> backbone();
}