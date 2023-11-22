package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tp;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;

import java.util.Map;

public class TPServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public TPServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public TPServiceHandler() {
        super();
    }

    public TPACleaningService tpaCleaningService() {
        return this.find(TPACleaningService.class).orElseThrow();
    }
}