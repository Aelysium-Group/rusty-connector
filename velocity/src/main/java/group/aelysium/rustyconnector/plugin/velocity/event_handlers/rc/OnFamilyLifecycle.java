package group.aelysium.rustyconnector.plugin.velocity.event_handlers.rc;

import group.aelysium.rustyconnector.common.events.EventListener;
import group.aelysium.rustyconnector.plugin.velocity.VirtualFamilyServers;
import group.aelysium.rustyconnector.proxy.events.FamilyCreateEvent;
import group.aelysium.rustyconnector.proxy.events.FamilyDeleteEvent;

public class OnFamilyLifecycle {
    protected final VirtualFamilyServers virtualFamilyServers;
    public OnFamilyLifecycle(VirtualFamilyServers virtualFamilyServers) {
        this.virtualFamilyServers = virtualFamilyServers;
    }

    @EventListener
    public void handle(FamilyCreateEvent event) {
        this.virtualFamilyServers.createNew(event.family());
    }

    @EventListener
    public void handle(FamilyDeleteEvent event) {
        this.virtualFamilyServers.delete(event.family().orElseThrow().id());
    }
}
