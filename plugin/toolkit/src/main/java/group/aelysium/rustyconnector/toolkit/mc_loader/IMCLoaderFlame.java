package group.aelysium.rustyconnector.toolkit.mc_loader;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.common.events.EventManager;
import group.aelysium.rustyconnector.toolkit.common.magic_link.IMagicLink;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLangLibrary;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;

import java.net.InetSocketAddress;
import java.util.UUID;

public interface IMCLoaderFlame extends Particle {
    /**
     * Gets the session uuid of this MCLoader.
     * The MCLoader's uuid won't change while it's alive, but once it's restarted or reloaded, the session uuid will change.
     * @return {@link UUID}
     */
    UUID uuid();

    /**
     * Gets the current version of RustyConnector
     * @return {@link Version}
     */
    Version version();

    /**
     * The display name of this MCLoader.
     */
    String displayName();

    /**
     * Gets the address of this server.
     * The address, assuming the user entered it properly, should be formatted in the same format as you format a joinable address in Velocity's velocity.toml.
     * @return {@link String}
     */
    InetSocketAddress address();

    /**
     * The number of players on this server.
     * @return {@link Integer}
     */
    int playerCount();

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

    /**
     * Sends a player to a specific MCLoader if it exists.
     * @param player The uuid of the player to send.
     * @param mcloader The uuid of the mcloader to send to.
     */
    void send(UUID player, UUID mcloader);

    Flux<IMagicLink.MCLoader> MagicLink();
    MCLoaderAdapter Adapter();
    Flux<MCLoaderLangLibrary> Lang();
    EventManager EventManager();

    abstract class Tinder extends Particle.Tinder<IMCLoaderFlame> {}
}