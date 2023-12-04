package group.aelysium.rustyconnector.toolkit.mc_loader.magic_link;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.mc_loader.packet_builder.IPacketBuilderService;

public interface IMagicLinkService<TPacketBuilderService extends IPacketBuilderService> extends Service {
    /**
     * Sets the status of this server's magic link.
     * Depending on the status this server may make requests to the proxy in different ways.
     * @param status The status to be set.
     */
    void setStatus(MagicLinkStatus status);

    /**
     * Set the ping delay for this upcoming ping.
     * @param delay The delay to set.
     */
    void setUpcomingPingDelay(int delay);

    /**
     * Starts the heartbeat that this server's magic link uses.
     * @param packetBuilderService The service used for issuing packets.
     */
    void startHeartbeat(TPacketBuilderService packetBuilderService);

    /**
     * Disconnects this server's magic link with the proxy.
     */
    void disconnect();
}
