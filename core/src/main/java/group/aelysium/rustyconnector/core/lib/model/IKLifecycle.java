package group.aelysium.rustyconnector.core.lib.model;

import java.util.HashMap;
import java.util.Map;

public abstract class IKLifecycle extends Serviceable {
    protected IKLifecycle(Map<Class<? extends Service>, Service> services) {
        super(services);
    }

    /**
     * Kill the object. Once this has been called the object should be guaranteed to be garbage collected.
     */
    protected abstract void kill();

    /**
     * Init the object.
     */
    protected static IKLifecycle init() {
        return null;
    }
}
