package group.aelysium.rustyconnector.plugin.velocity.lib;

import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnection;
import group.aelysium.rustyconnector.core.lib.connectors.messenger.MessengerConnector;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;

import java.util.Map;

/**
 * The core module of RustyConnector.
 * All aspects of the plugin should be accessible from here.
 * If not, check {@link VelocityAPI}.
 */
public class Core extends ServiceableService<CoreServiceHandler> {
    /**
     * The core message backbone where all RC messages are sent through.
     */
    private final MessengerConnector<? extends MessengerConnection> backbone;

    public Core(Map<Class<? extends Service>, Service> services, String backboneConnector) {
        super(new CoreServiceHandler(services));
        this.backbone = (MessengerConnector<? extends MessengerConnection>) this.services().connectorsService().get(backboneConnector);
    }

    public MessengerConnector<? extends MessengerConnection> backbone() {
        return this.backbone;
    }
}
