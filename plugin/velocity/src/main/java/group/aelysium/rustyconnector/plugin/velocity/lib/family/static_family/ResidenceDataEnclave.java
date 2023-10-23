package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ResolvableFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.ResolvablePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.MySQLStorage;
import group.aelysium.rustyconnector.plugin.velocity.lib.storage.StorageRoot;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ResidenceDataEnclave {
    private final MySQLStorage storage;

    public ResidenceDataEnclave(MySQLStorage storage) {
        this.storage = storage;
    }

    public Optional<ServerResidence> fetch(Player player, StaticServerFamily family) {
        try {
            StorageRoot root = this.storage.root();

            Optional<ServerResidence> serverResidence = root.residence().stream()
                .filter(residence ->
                    residence.rawPlayer().equals(ResolvablePlayer.from(player)) &&
                    residence.rawFamily().equals(ResolvableFamily.from(family))
                )
                .findAny();

            return serverResidence;
        } catch (NoSuchElementException ignore) {}
        catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
    public void save(Player player, PlayerServer server, StaticServerFamily family)  {
        StorageRoot root = this.storage.root();

        ServerResidence serverResidence = new ServerResidence(player, server, family, family.homeServerExpiration());

        List<ServerResidence> residences = root.residence();
        residences.add(serverResidence);
        this.storage.store(residences);
    }
    public void delete(Player player, StaticServerFamily family) {
        StorageRoot root = this.storage.root();

        List<ServerResidence> residences = root.residence();
        residences.removeIf(residence ->
                residence.rawPlayer().equals(ResolvablePlayer.from(player)) &&
                residence.rawFamily().equals(ResolvableFamily.from(family))
        );

        this.storage.store(residences);
    }

    public void updateExpirations(LiquidTimestamp expiration, StaticServerFamily family) throws Exception {
        if(expiration == null)
            updateValidExpirations(family);
        else
            updateNullExpirations(family);
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     */
    protected void purgeExpired(StaticServerFamily family) {
        StorageRoot root = this.storage.root();

        List<ServerResidence> residenceList = root.residence();
        residenceList.removeIf(serverResidence ->
                serverResidence.expiration() < Instant.EPOCH.getEpochSecond() &&
                serverResidence.rawFamily().equals(ResolvableFamily.from(family))
        );

        this.storage.store(residenceList);
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     */
    protected void updateNullExpirations(StaticServerFamily family) {
        StorageRoot root = this.storage.root();

        List<ServerResidence> residenceList = root.residence();
        residenceList.forEach(serverResidence -> {
            if(!serverResidence.rawFamily().equals(ResolvableFamily.from(family))) return;
            serverResidence.expiration(null);
        });

        this.storage.store(residenceList);
    }

    /**
     * If any home servers are set to expire, and if an expiration time is disabled in the family,
     * This will update all expirations to now never expire;
     * @param family The family to search in.
     */
    protected void updateValidExpirations(StaticServerFamily family) {
        StorageRoot root = this.storage.root();

        List<ServerResidence> residenceList = root.residence();
        residenceList.forEach(serverResidence -> {
            if(!serverResidence.rawFamily().equals(ResolvableFamily.from(family))) return;
            serverResidence.expiration(family.homeServerExpiration());
        });

        this.storage.store(residenceList);
    }

}
