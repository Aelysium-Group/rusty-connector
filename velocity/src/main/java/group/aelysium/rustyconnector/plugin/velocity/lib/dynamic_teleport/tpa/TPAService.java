package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa;

import com.typesafe.config.Optional;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;

import java.util.Map;

public class TPAService extends ServiceableService {
    public TPAService(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    @Override
    public void kill() {
    }

    /**
     * The services that are valid for this service provider.
     * Services marked as @Optional should be handled accordingly.
     * If a service is not marked @Optional it should be impossible for that service to be unavailable.
     */
    public static class ValidServices {
        public static Class<TPACleaningService> TPA_CLEANING_SERVICE = TPACleaningService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            return false;
        }
    }
}
