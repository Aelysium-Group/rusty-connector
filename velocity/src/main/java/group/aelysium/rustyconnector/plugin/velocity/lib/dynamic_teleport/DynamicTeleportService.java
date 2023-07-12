package group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport;

import com.typesafe.config.Optional;
import group.aelysium.rustyconnector.core.lib.model.Service;
import group.aelysium.rustyconnector.core.lib.model.ServiceableService;
import group.aelysium.rustyconnector.plugin.velocity.lib.dynamic_teleport.tpa.TPAService;

import java.util.Map;

public class DynamicTeleportService extends ServiceableService {
    public DynamicTeleportService(Map<Class<? extends Service>, Service> services) {
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
        @Optional
        public static Class<TPAService> TPA_SERVICE = TPAService.class;

        public static boolean isOptional(Class<? extends Service> clazz) {
            if(clazz == TPA_SERVICE) return true;

            return false;
        }
    }
}
