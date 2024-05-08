package group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors;

import com.google.gson.JsonObject;
import group.aelysium.rustyconnector.toolkit.core.serviceable.interfaces.Service;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IRankResolver;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Module containing valid Storage queries that can be used to access database information.
 */
public abstract class StorageReactor implements Service {
    public abstract void initializeDatabase();

    public abstract void updateExpirations(String familyId, Long newExpirationEpoch);
    public abstract void purgeExpiredServerResidences();
    public abstract Optional<IServerResidence.MCLoaderEntry> fetchServerResidence(String familyId, UUID player);
    public abstract void deleteServerResidence(String familyId, UUID player);
    public abstract void deleteServerResidences(String familyId);
    public abstract void saveServerResidence(String familyId, UUID mcloader, UUID player, Long expirationEpoch);
    public abstract void deleteFriendLink(PlayerPair pair);
    public abstract Optional<Boolean> areFriends(PlayerPair pair);
    public abstract Optional<List<IPlayer>> fetchFriends(UUID player);
    public abstract void saveFriendLink(PlayerPair pair);
    public abstract Optional<IPlayer> fetchPlayer(UUID uuid);
    public abstract Optional<IPlayer> fetchPlayer(String username);
    public abstract void savePlayer(UUID uuid, String username);
    public abstract void deleteGame(String gameId);
    public abstract void deleteRank(UUID player);
    public abstract void deleteRank(UUID player, String gameId);
    public abstract void saveRank(UUID player, String gameId, JsonObject rank);
    public abstract void purgeInvalidSchemas(String gameId, String validSchema);
    public abstract Optional<IVelocityPlayerRank> fetchRank(UUID player, String gameId, IRankResolver resolver);

    public static abstract class Holder {
        protected final StorageReactor reactor;
        public Holder(StorageReactor reactor) {
            this.reactor = reactor;
        }
    }
}
