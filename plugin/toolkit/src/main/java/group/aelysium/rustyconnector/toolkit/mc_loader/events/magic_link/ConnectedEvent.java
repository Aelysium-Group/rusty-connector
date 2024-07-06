package group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link;

import group.aelysium.rustyconnector.toolkit.common.events.Event;
import group.aelysium.rustyconnector.toolkit.common.server.ServerAssignment;

public class ConnectedEvent implements Event {
    private final ServerAssignment assignment;

    public ConnectedEvent(ServerAssignment assignment) {
        this.assignment = assignment;
    }

    public ServerAssignment assignment() {
        return this.assignment;
    }
}
