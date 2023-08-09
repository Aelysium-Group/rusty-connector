package group.aelysium.rustyconnector.plugin.velocity.lib.viewport;


import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.AuthenticationService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.PrintingService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.RequestResolverService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.ViewportMySQLService;
import group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services.gateway.GatewayService;

import java.util.Map;

public class ViewportServiceHandler extends group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler {
    public ViewportServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public ViewportServiceHandler() {
        super();
    }

    public ViewportMySQLService mySQLService() {
        return this.find(ViewportMySQLService.class).orElseThrow();
    }
    public PrintingService printingService() {
        return this.find(PrintingService.class).orElseThrow();
    }
    public RequestResolverService requestResolverService() {
        return this.find(RequestResolverService.class).orElseThrow();
    }
    public AuthenticationService authenticationService() {
        return this.find(AuthenticationService.class).orElseThrow();
    }
    public GatewayService gatewayService() {
        return this.find(GatewayService.class).orElseThrow();
    }
}