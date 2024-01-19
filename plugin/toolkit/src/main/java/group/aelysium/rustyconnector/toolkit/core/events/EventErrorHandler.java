package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

public class EventErrorHandler implements IPublicationErrorHandler {
    @Override
    public void handleError(PublicationError error) {
        error.getCause().printStackTrace();
    }
}
