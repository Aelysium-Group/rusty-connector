package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.injectors.InjectorService;
import group.aelysium.rustyconnector.toolkit.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.toolkit.velocity.dynamic_teleport.IDynamicTeleportServiceHandler;

import java.util.Map;
import java.util.Optional;

public class DynamicTeleportServiceHandler extends ServiceHandler implements IDynamicTeleportServiceHandler {
    public DynamicTeleportServiceHandler(Map<Class<? extends Service>, Service> services) {
        super(services);
    }
    public DynamicTeleportServiceHandler() {
        super();
    }

    public Optional<TPAService> tpa() {
        return this.find(TPAService.class);
    }
    public Optional<AnchorService> anchor() {
        return this.find(AnchorService.class);
    }
    public Optional<InjectorService> injector() {
        return this.find(InjectorService.class);
    }
    public Optional<HubService> hub() {
        return this.find(HubService.class);
    }
}