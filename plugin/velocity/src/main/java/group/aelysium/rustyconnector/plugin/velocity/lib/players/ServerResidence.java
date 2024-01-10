package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.Family;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import org.eclipse.serializer.collections.lazy.LazyHashMap;

import java.time.Instant;
import java.util.*;

public interface ServerResidence {
    /**
     * Stores the new server residence.
     * This method will create new Map entries if some don't already exist.
     */
    static void store(IStaticFamily family, IMCLoader server, IPlayer player) {
        StorageService storage = Tinder.get().services().storage();
        Database database = storage.database();

        Map<UUID, IServerResidence.MCLoaderEntry> familyMap = database.residence().get(family.id());
        if(familyMap == null) {
            familyMap = new LazyHashMap<>();
            storage.store(database.residence());
        }

        LiquidTimestamp liquidExpiration = family.homeServerExpiration();
        Long expiration = null;
        if(liquidExpiration != null) expiration = liquidExpiration.epochFromNow();

        familyMap.put(player.uuid(), new IServerResidence.MCLoaderEntry(server.uuid(), expiration));
        storage.store(familyMap);
    }

    /**
     * Deletes the server residences for an entire family.
     */
    static void delete(IStaticFamily family) {
        StorageService storage = Tinder.get().services().storage();
        Database database = storage.database();

        database.residence().remove(family.id());

        storage.store(database.residence());
    }

    /**
     * Deletes the server residence for a specific family.
     */
    static void delete(IStaticFamily family, UUID player) {
        StorageService storage = Tinder.get().services().storage();

        Map<UUID, IServerResidence.MCLoaderEntry> residences = storage.database().residence().get(family.id());
        if(residences == null) return;

        residences.remove(player);

        storage.store(residences);
    }

    /**
     * Fetches a ServerResidence for the player.
     * If no residence exists for the player, it creates one and returns it.
     * @param uuid The UUID of the player to look for.
     */
    static Optional<IServerResidence.MCLoaderEntry> fetch(IStaticFamily family, UUID uuid) {
        Database database = Tinder.get().services().storage().database();

        Map<UUID, IServerResidence.MCLoaderEntry> residence = database.residence().get(family);
        if (residence == null) return Optional.empty();

        IServerResidence.MCLoaderEntry mcLoaderEntry = residence.get(uuid);
        if(mcLoaderEntry == null) return Optional.empty();

        return Optional.of(mcLoaderEntry);
    }

    static void updateExpirations(LiquidTimestamp expiration, IStaticFamily family) throws Exception {
        StorageService storage = Tinder.get().services().storage();
        Database database = storage.database();

        Map<UUID, IServerResidence.MCLoaderEntry> residences = database.residence().get(family.id());
        if(residences == null) return;

        if(expiration == null) // Update Valid Expirations
            residences.forEach((key, value) -> value.expiration(family.homeServerExpiration()));
        else
            residences.forEach((key, value) -> value.expiration(null));

        storage.store(residences);
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     */
    static void purgeExpired(IStaticFamily family) {
        StorageService storage = Tinder.get().services().storage();
        Database database = storage.database();

        Map<UUID, IServerResidence.MCLoaderEntry> residences = database.residence().get(family.id());
        residences.values().removeIf(IServerResidence.MCLoaderEntry::expired);

        storage.store(residences);
    }
}