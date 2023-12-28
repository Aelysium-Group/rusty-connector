package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IResidenceDataEnclave;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class ResidenceDataEnclave implements IResidenceDataEnclave<MCLoader, Player, StaticFamily> {
    private final StorageService storage;

    public ResidenceDataEnclave(StorageService storage) {
        this.storage = storage;
    }

    public Optional<ServerResidence> fetch(Player.Reference player, StaticFamily family) {
        try {
            Database root = this.storage.root();

            return root.residence().stream()
                .filter(residence ->
                    residence.rawPlayer().equals(player) &&
                    residence.family().equals(family)
                )
                .findAny();
        } catch (NoSuchElementException ignore) {}
        catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
    public void save(Player.Reference player, MCLoader server, StaticFamily family)  {
        Database root = this.storage.root();

        ServerResidence serverResidence = new ServerResidence(player, server, family, family.homeServerExpiration());

        Set<ServerResidence> residences = root.residence();
        residences.add(serverResidence);
        this.storage.store(residences);
    }
    public void delete(Player.Reference player, StaticFamily family) {
        Database root = this.storage.root();

        Set<ServerResidence> residences = root.residence();
        residences.removeIf(residence ->
                residence.rawPlayer().equals(player) &&
                residence.family().equals(family)
        );

        this.storage.store(residences);
    }

    public void updateExpirations(LiquidTimestamp expiration, StaticFamily family) throws Exception {
        if(expiration == null)
            updateValidExpirations(family);
        else
            updateNullExpirations(family);
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     */
    public void purgeExpired(StaticFamily family) {
        Database root = this.storage.root();

        Set<ServerResidence> residenceList = root.residence();
        residenceList.removeIf(serverResidence ->
                serverResidence.expiration() < Instant.EPOCH.getEpochSecond() &&
                serverResidence.family().equals(family)
        );

        this.storage.store(residenceList);
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     */
    protected void updateNullExpirations(StaticFamily family) {
        Database root = this.storage.root();

        Set<ServerResidence> residenceList = root.residence();
        residenceList.forEach(serverResidence -> {
            if(!serverResidence.family().equals(family)) return;
            serverResidence.expiration(null);
        });

        this.storage.store(residenceList);
    }

    /**
     * If any home servers are set to expire, and if an expiration time is disabled in the family,
     * This will update all expirations to now never expire;
     * @param family The family to search in.
     */
    protected void updateValidExpirations(StaticFamily family) {
        Database root = this.storage.root();

        Set<ServerResidence> residenceList = root.residence();
        residenceList.forEach(serverResidence -> {
            if(!serverResidence.family().equals(family)) return;
            serverResidence.expiration(family.homeServerExpiration());
        });

        this.storage.store(residenceList);
    }

}
