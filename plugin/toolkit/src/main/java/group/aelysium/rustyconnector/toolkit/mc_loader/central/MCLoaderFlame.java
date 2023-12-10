package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;

public abstract class MCLoaderFlame<TCoreServiceHandler extends ICoreServiceHandler, TMessengerConnector extends IMessengerConnector<?>> extends ServiceableService<TCoreServiceHandler> {

    public MCLoaderFlame(TCoreServiceHandler services) {
        super(services);
    }

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public abstract String versionAsString();

    /**
     * Exhaust the current instance of the RustyConnector kernel.
     * A new Flame can be created using {@link MCLoaderTinder#ignite()}
     */
    public abstract void exhaust();

    /**
     * Gets RustyConnector's backbone messenger service.
     * @return {@link TMessengerConnector}
     */
    public abstract TMessengerConnector backbone();
}