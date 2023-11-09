package group.aelysium.rustyconnector.core.lib.messenger;

import group.aelysium.rustyconnector.toolkit.core.packet.PacketOrigin;

public abstract class MessengerConnection {
    protected PacketOrigin origin;
    public MessengerConnection(PacketOrigin origin) {
        this.origin = origin;
    }
}