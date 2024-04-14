package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.IServiceableService;
import group.aelysium.rustyconnector.toolkit.velocity.util.Version;

import java.util.UUID;

public interface IMCLoaderFlame<TCoreServiceHandler extends ICoreServiceHandler> extends IServiceableService<TCoreServiceHandler> {
    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    String versionAsString();

    /**
     * Locks this MCLoader so that players can't join it via the family's load balancer.
     */
    void lock();

    /**
     * Unlocks this MCLoader so that players can join it via the family's load balancer.
     */
    void unlock();

    /**
     * Sends a player to a specific family if it exists.
     * @param player The uuid of the player to send.
     * @param familyID The id of the family to send to.
     */
    void send(UUID player, String familyID);
}