package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;

public abstract class MCLoaderFlame<TCoreServiceHandler extends ICoreServiceHandler, TMessengerConnection extends IMessengerConnection<?>, TMessengerConnector extends IMessengerConnector<?>> extends ServiceableService<TCoreServiceHandler> {

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