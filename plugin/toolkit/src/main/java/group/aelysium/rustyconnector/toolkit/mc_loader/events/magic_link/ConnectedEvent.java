package group.aelysium.rustyconnector.toolkit.mc_loader.events.magic_link;

import group.aelysium.rustyconnector.toolkit.core.events.Cancelable;
import group.aelysium.rustyconnector.toolkit.core.server.ServerAssignment;

public class ConnectedEvent extends Cancelable {
    private final ServerAssignment assignment;

    public ConnectedEvent(ServerAssignment assignment) {
        this.assignment = assignment;
    }

    public ServerAssignment assignment() {
        return this.assignment;
    }
}
