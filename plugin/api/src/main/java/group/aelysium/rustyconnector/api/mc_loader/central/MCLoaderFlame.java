package group.aelysium.rustyconnector.api.mc_loader.central;

import group.aelysium.rustyconnector.api.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.api.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.api.velocity.util.Version;

public abstract class MCLoaderFlame<TCoreServiceHandler extends ICoreServiceHandler, TMessengerConnection extends IMessengerConnection<?, ?, ?>, TMessengerConnector extends IMessengerConnector<?, ?, ?>> extends ServiceableService<TCoreServiceHandler> {

    public MCLoaderFlame(TCoreServiceHandler services) {
        super(services);
    }

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public abstract String versionAsString();

    public abstract void exhaust();
    public abstract TMessengerConnector backbone();
}