package group.aelysium.rustyconnector.toolkit.velocity.central;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import net.kyori.adventure.text.Component;

import java.util.*;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link VelocityTinder}.
 */
public abstract class VelocityFlame<TCoreServiceHandler extends ICoreServiceHandler> extends ServiceableService<TCoreServiceHandler> {
    public VelocityFlame(TCoreServiceHandler services) {
        super(services);
    }

    /**
     * Gets the session uuid of this Proxy.
     * The Proxy's uuid won't change while it's alive, but once it's restarted or reloaded, the session uuid will change.
     * @return {@link UUID}
     */
    public abstract UUID uuid();

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public abstract Version version();

    /**
     * Gets RustyConnector's boot log.
     * The log represents all the debug messages sent during the boot or reboot of RustyConnector.
     * The log is in the same order of when the logs were sent.
     * @return {@link List<Component>}
     */
    public abstract List<Component> bootLog();
}