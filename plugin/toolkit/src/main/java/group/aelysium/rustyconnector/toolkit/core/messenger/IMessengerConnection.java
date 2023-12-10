package group.aelysium.rustyconnector.toolkit.core.messenger;

import group.aelysium.rustyconnector.toolkit.core.packet.IPacket;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface IMessengerConnection extends Service {
    /**
     * Publish a new packet to the {@link IMessengerConnection}.
     *
     * @param packet The packet to publish.
     */
    <TPacket extends IPacket> void publish(TPacket packet);

    /**
     * Register a listener to handle particular packets.
     * @param listener The listener to use.
     */
    void listen(PacketListener listener);
}
