package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPASettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicTeleportService extends ServiceableService<DynamicTeleportServiceHandler> {
    protected DynamicTeleportService(Map<Class<? extends Service>, Service> services) {
        super(new DynamicTeleportServiceHandler(services));
    }

    public static Optional<DynamicTeleportService> init(DynamicTeleportConfig config) {
        if(!config.isEnabled()) return Optional.empty();

        try {
            DynamicTeleportService.Builder builder = new DynamicTeleportService.Builder();

            if(config.isTpa_enabled()) {
                TPASettings tpaSettings = new TPASettings(
                        config.isTpa_friendsOnly(),
                        config.isTpa_ignorePlayerCap(),
                        config.getTpa_expiration(),
                        config.getTpa_enabledFamilies()
                );

                TPAService tpaService = new TPAService(tpaSettings);

                builder.addService(tpaService);
            }

            if(config.isFamilyAnchor_enabled()) {
                try {
                    builder.addService(AnchorService.init(config).orElseThrow());
                } catch (Exception ignore) {}
            }

            if(config.isHub_enabled()) {
                try {
                    builder.addService(new HubService(config.getHub_enabledFamilies()));
                } catch (Exception ignore) {}
            }

            return Optional.of(builder.build());
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    protected static class Builder {
        protected final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public DynamicTeleportService.Builder addService(Service service) {
            this.services.put(service.getClass(), service);
            return this;
        }

        public DynamicTeleportService build() {
            return new DynamicTeleportService(this.services);
        }
    }
}