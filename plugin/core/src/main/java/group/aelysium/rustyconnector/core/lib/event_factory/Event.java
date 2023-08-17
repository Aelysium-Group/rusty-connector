package group.aelysium.rustyconnector.core.lib.event_factory;

import java.util.HashMap;
import java.util.Map;

public abstract class Event {
    protected String name;

    /**
     * Empty constructor to allow for {@link Listener#invoking()} to be called.
     */
    public Event() {}

    public String name() {
        return this.name;
    }
}
