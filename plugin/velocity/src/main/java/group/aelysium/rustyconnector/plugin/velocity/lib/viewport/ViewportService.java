package group.aelysium.rustyconnector.plugin.velocity.lib.viewport;

import group.aelysium.rustyconnector.api.velocity.lib.serviceable.Service;
import group.aelysium.rustyconnector.api.velocity.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.config.ViewportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.rest.APIService;

import java.util.HashMap;
import java.util.Map;

public class ViewportService extends ServiceableService<ViewportServiceHandler> {
    protected ViewportService(Map<Class<? extends Service>, Service> services) {
        super(new ViewportServiceHandler(services));
    }

    public static ViewportService create(ViewportConfig config) {
        Map<Class<? extends Service>, Service> services = new HashMap<>();
        services.put(APIService.class, new APIService(config.getApi_address(), new APIService.APISettings(config.getAfkExpiration(), config.getCredentials())));

        return new ViewportService(services);
    }
}
