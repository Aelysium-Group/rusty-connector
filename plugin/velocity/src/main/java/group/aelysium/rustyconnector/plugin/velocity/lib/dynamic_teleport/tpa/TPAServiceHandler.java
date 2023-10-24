package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;

import java.util.Map;

public class TPAServiceHandler extends ServiceHandler {
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