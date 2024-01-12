package group.aelysium.rustyconnector.toolkit.core.events;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.subscription.SubscriptionContext;

public class CancelFilter implements IMessageFilter<Object> {
    @Override
    public boolean accepts(Object message, SubscriptionContext context) {
        if (message instanceof Cancelable)
            return !((Cancelable) message).isCanceled();

        return true;
    }
}