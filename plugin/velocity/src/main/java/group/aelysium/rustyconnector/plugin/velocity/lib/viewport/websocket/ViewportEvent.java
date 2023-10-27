package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.websocket;

import group.aelysium.rustyconnector.core.lib.event_factory.Event;

public abstract class ViewportEvent extends Event {
    public abstract String toJsonPacket();

    @Override
    public String toString() {
        return this.toJsonPacket();
    }
}
