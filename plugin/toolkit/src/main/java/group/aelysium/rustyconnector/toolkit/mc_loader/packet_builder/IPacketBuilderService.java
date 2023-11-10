package group.aelysium.rustyconnector.toolkit.mc_loader.packet_builder;

import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.mc_loader.connection_intent.ConnectionIntent;

import java.util.UUID;

public interface IPacketBuilderService extends Service {
    /**
     * Issues a ping request to the proxy.
     * @param intent The intention of the ping.
     */
    void pingProxy(ConnectionIntent intent);

    /**
     * Issues a request to send a player to a different family.
     * @param player The player to send.
     * @param familyName The name of the family to send them to.
     */
    void sendToOtherFamily(UUID player, String familyName);

    /**
     * Issues a request to unlock this server.
     */
    void unlockServer();

    /**
     * Issues a request to lock this server.
     */
    void lockServer();

    /**
     * Issues a request for the proxy to end a ranked game associated with this server, if it exists.
     * @param uuid The uuid of the game.
     */
    void endRankedGame(UUID uuid);
}
