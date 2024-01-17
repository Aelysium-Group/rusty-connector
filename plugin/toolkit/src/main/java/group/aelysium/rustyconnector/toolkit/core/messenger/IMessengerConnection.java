package group.aelysium.rustyconnector.toolkit.core.messenger;

import group.aelysium.rustyconnector.toolkit.core.packet.Packet;
import group.aelysium.rustyconnector.toolkit.core.packet.PacketListener;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;

public interface IMessengerConnection extends Service {
    /**
     * Publish a new packet to the {@link IMessengerConnection}.
     * @param packet The packet to publish.
     */
    void publish(Packet packet);
    /**
     * Publish a new packet to the {@link IMessengerConnection}.
     * This method is identical to calling {@link #publish(Packet) .publish(}{@link Packet.Wrapper#packet()}{@link #publish(Packet) )}
     * @param packet The packet wrapper to publish.
     */
    void publish(Packet.Wrapper packet);

    /**
     * Register a listener to handle particular packets.
     * @param listener The listener to use.
     */
    <TPacketListener extends PacketListener<? extends Packet.Wrapper>> void listen(TPacketListener listener);
}
