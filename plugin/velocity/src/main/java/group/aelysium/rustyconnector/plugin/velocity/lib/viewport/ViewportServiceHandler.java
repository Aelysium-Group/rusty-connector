package group.aelysium.rustyconnector.plugin.velocity.lib.viewport;


import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.api.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;

import java.util.Map;

public class ViewportServiceHandler extends ServiceHandler {
    public ViewportServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public ViewportServiceHandler() {
        super();
    }
    public APIService api() {
        return this.find(APIService.class).orElseThrow();
    }
}