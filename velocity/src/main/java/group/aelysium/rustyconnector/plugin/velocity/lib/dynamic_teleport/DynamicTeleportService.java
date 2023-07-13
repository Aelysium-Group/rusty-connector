package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import group.aelysium.rustyconnector.core.lib.exception.NoOutputException;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.config.DynamicTeleportConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPASettings;

import java.util.HashMap;
import java.util.Optional;

// TODO: Currently pretty bare bones, expansions are planned
public class DynamicTeleportService extends ServiceableService {
    protected DynamicTeleportService() {
        super(new HashMap<>());
    }

    protected DynamicTeleportService(TPAService tpaService) {
        super(new HashMap<>());

        this.services.put(TPAService.class, tpaService);
    }

    public static Optional<DynamicTeleportService> init(DynamicTeleportConfig config) {
        try {
            if(!config.isEnabled()) throw new NoOutputException();

            // TODO: Currently works as a guard clause, needs to be changed as new modules are added to Dynamic Teleport
            if(!config.isTpa_enabled())
                return Optional.of(new DynamicTeleportService());

            TPASettings tpaSettings = new TPASettings(
                    config.isTpa_friendsOnly(),
                    config.isTpa_ignorePlayerCap(),
                    config.getTpa_expiration(),
                    config.getTpa_enabledFamilies()
            );

            TPAService tpaService = new TPAService(tpaSettings);

            DynamicTeleportService dynamicTeleportService = new DynamicTeleportService(tpaService);

            return Optional.of(dynamicTeleportService);
        } catch (Exception ignore) {}

        return Optional.empty();
    }

    /**
     * The services that are valid for this service provider.
     * Services marked as @Optional should be handled accordingly.
     * If a service is not marked @Optional it should be impossible for that service to be unavailable.
     */
    public static class ValidServices {
        public static Class<TPAService> TPA_SERVICE = TPAService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            if(clazz == TPA_SERVICE) return true;

            return false;
        }
    }
}
