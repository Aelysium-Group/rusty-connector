package group.aelysium.rustyconnector.toolkit.premier.central;

import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityTinder;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * The core RustyConnector kernel.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link VelocityTinder}.
 */
public abstract class PremierFlame<TCoreServiceHandler extends ICoreServiceHandler> extends ServiceableService<TCoreServiceHandler> {
    public PremierFlame(TCoreServiceHandler services) {
        super(services);
    }

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    public abstract Version version();

    /**
     * Gets the current version being used by RustyConnector's config manager.
     * @return {@link Integer}
     */
    public abstract int configVersion();

    /**
     * Gets RustyConnector's boot log.
     * The log represents all the debug messages sent during the boot or reboot of RustyConnector.
     * The log is in the same order of when the logs were sent.
     * @return {@link List<Component>}
     */
    public abstract List<Component> bootLog();
}