package group.aelysium.rustyconnector.toolkit.velocity.family.static_family;

import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface IServerResidence {
    IPlayer player();

    Optional<MCLoaderEntry> server(String familyID);

    /**
     * Deletes all mappings that are expired.
     */
    void purgeExpired();

    class MCLoaderEntry {
        protected UUID mcloaderUUID;
        protected Long expiration = null;

        public MCLoaderEntry(UUID mcloaderUUID, Long expiration) {
            this.mcloaderUUID = mcloaderUUID;
            this.expiration = expiration;
        }

        public IMCLoader server() {
            return new IMCLoader.Reference(this.mcloaderUUID).get();
        }

        public boolean expired() {
            if(expiration == null) return false;
            return expiration < Instant.EPOCH.getEpochSecond();
        }

        public void expiration(LiquidTimestamp expiration) {
            if(expiration == null) this.expiration = null;
            else this.expiration = expiration.epochFromNow();
        }

    }
}