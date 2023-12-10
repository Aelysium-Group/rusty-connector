package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;

public interface MCLoaderFlame<TCoreServiceHandler extends ICoreServiceHandler, TMessengerConnector extends IMessengerConnector<?>> extends IServiceableService<TCoreServiceHandler> {
    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    String versionAsString();

    /**
     * Gets RustyConnector's backbone messenger service.
     * @return {@link TMessengerConnector}
     */
    TMessengerConnector backbone();
}