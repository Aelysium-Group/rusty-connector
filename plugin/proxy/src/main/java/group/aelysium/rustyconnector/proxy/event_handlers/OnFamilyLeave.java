package group.aelysium.rustyconnector.proxy.event_handlers;

import group.aelysium.rustyconnector.toolkit.common.events.Listener;
import group.aelysium.rustyconnector.toolkit.proxy.events.player.FamilyLeaveEvent;

public class OnFamilyLeave implements Listener<FamilyLeaveEvent> {
    public void handler(FamilyLeaveEvent event) {
        try {
            event.family().orElseThrow().leave(event.player());
        } catch (Exception ignore) {}
    }
}