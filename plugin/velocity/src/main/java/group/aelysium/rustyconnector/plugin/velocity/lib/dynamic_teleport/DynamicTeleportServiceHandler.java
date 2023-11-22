package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tp.TPService;

import java.util.Map;
import java.util.Optional;

public class DynamicTeleportServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public DynamicTeleportServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public DynamicTeleportServiceHandler() {
        super();
    }

    public Optional<TPService> tpService() {
        return this.find(TPService.class);
    }
    public Optional<AnchorService> anchorService() {
        return this.find(AnchorService.class);
    }
    public Optional<HubService> hubService() {
        return this.find(HubService.class);
    }
}