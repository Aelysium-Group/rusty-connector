package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.anchors.AnchorService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.hub.HubService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPASettings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DynamicTeleportService extends ServiceableService {
    protected DynamicTeleportService(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    public static Optional<DynamicTeleportService> init(DynamicTeleportConfig config) {
        try {
            if(!config.isEnabled()) throw new NoOutputException();

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

    /**
     * The services that are valid for this service provider.
     * Services marked as @Optional should be handled accordingly.
     * If a service is not marked @Optional it should be impossible for that service to be unavailable.
     */
    public static class ValidServices {
        public static Class<TPAService> TPA_SERVICE = TPAService.class;
        public static Class<AnchorService> ANCHOR_SERVICE = AnchorService.class;
        public static Class<HubService> HUB_SERVICE = HubService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            if(clazz == TPA_SERVICE) return true;

            return false;
        }
    }
}
