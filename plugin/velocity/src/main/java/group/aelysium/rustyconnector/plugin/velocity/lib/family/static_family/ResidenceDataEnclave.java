package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IResidenceDataEnclave;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IStaticFamily;
import group.aelysium.rustyconnector.toolkit.velocity.players.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageService;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.Database;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public class ResidenceDataEnclave implements IResidenceDataEnclave {
    private final StorageService storage;

    public ResidenceDataEnclave(StorageService storage) {
        this.storage = storage;
    }

    public Optional<ServerResidence> fetch(IPlayer.Reference player, IStaticFamily family) {
        try {
            Database root = this.storage.database();

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
    public void save(IPlayer.Reference player, IMCLoader server, IStaticFamily family)  {
        Database root = this.storage.database();

        ServerResidence serverResidence = new ServerResidence(player, new IMCLoader.Reference(server.uuid()), new IFamily.Reference(family.id()), family.homeServerExpiration());

        Set<ServerResidence> residences = root.residence();
        residences.add(serverResidence);
        this.storage.store(residences);
    }
    public void delete(IPlayer.Reference player, IStaticFamily family) {
        Database root = this.storage.database();

        Set<ServerResidence> residences = root.residence();
        residences.removeIf(residence ->
                residence.rawPlayer().equals(player) &&
                residence.family().equals(family)
        );

        this.storage.store(residences);
    }

    public void updateExpirations(LiquidTimestamp expiration, IStaticFamily family) throws Exception {
        if(expiration == null)
            updateValidExpirations(family);
        else
            updateNullExpirations(family);
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     */
    public void purgeExpired(IStaticFamily family) {
        Database root = this.storage.database();

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
    protected void updateNullExpirations(IStaticFamily family) {
        Database root = this.storage.database();

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
    protected void updateValidExpirations(IStaticFamily family) {
        Database root = this.storage.database();

        Set<ServerResidence> residenceList = root.residence();
        residenceList.forEach(serverResidence -> {
            if(!serverResidence.family().equals(family)) return;
            serverResidence.expiration(family.homeServerExpiration());
        });

        this.storage.store(residenceList);
    }

}
