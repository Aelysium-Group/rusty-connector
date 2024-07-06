package group.aelysium.rustyconnector.toolkit.common.events;

import java.util.concurrent.RecursiveTask;

public class AsyncPublishCommand extends RecursiveTask<Boolean> {
    private final Event event;
    private final Listener<Event> listener;

    public AsyncPublishCommand(Event event, Listener<Event> listener) {
        this.event = event;
        this.listener = listener;
    }

    @Override
    public Boolean compute() {
        try {
            listener.handler(this.event);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
