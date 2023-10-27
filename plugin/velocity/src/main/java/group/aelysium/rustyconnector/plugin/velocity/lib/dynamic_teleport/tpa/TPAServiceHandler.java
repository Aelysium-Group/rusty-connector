package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;

import java.util.Map;
import java.util.Optional;

public class TPAServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public TPAServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public TPAServiceHandler() {
        super();
    }

    public TPACleaningService tpaCleaningService() {
        return this.find(TPACleaningService.class).orElseThrow();
    }
}