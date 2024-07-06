package group.aelysium.rustyconnector.proxy.remote_storage.reactors;

import group.aelysium.rustyconnector.toolkit.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;

import java.util.Optional;
import java.util.UUID;

/**
 * Module containing valid Storage queries that can be used to access database information.
 */
public interface StorageReactor extends Particle {
    void initializeDatabase();

    Optional<IPlayer> fetchPlayer(UUID uuid);
    Optional<IPlayer> fetchPlayer(String username);
    void savePlayer(UUID uuid, String username);
    abstract class Holder {
        protected final StorageReactor reactor;
        protected Holder(StorageReactor reactor) {
            this.reactor = reactor;
        }
    }
}