package group.aelysium.rustyconnector.toolkit.proxy.storage;

import group.aelysium.rustyconnector.toolkit.proxy.family.mcloader.MCLoader;
import group.aelysium.rustyconnector.toolkit.proxy.player.IPlayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local storage is the internal memory solution used for short-term internal RustyConnector data.
 * Local storage will not persist between software restarts since it's stored in RAM.
 */
public class LocalStorage {
    private final MCLoaders mcloaders = new MCLoaders();
    private final Players players = new Players();

    public MCLoaders mcloaders() {
        return this.mcloaders;
    }
    
    public Players players() {
        return this.players;
    }

    public void close() throws Exception {
        this.mcloaders.close();
        this.players.close();
    }

    public static class Players {
        private final Map<UUID, IPlayer> players = new LinkedHashMap<>(100){
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return this.size() > 100;
            }
        };

        public void store(UUID uuid, IPlayer player) {
            this.players.put(uuid, player);
        }

        public Optional<IPlayer> fetch(UUID uuid) {
            return Optional.ofNullable(this.players.get(uuid));
        }

        public void remove(UUID uuid) {
            this.players.remove(uuid);
        }

        public void close() throws Exception {
            this.players.clear();
        }
    }

    public static class MCLoaders {
        private final Map<UUID, MCLoader> mcloaders = new ConcurrentHashMap<>();

        public void store(UUID uuid, MCLoader mcloader) {
            this.mcloaders.put(uuid, mcloader);
        }

        public Optional<MCLoader> fetch(UUID uuid) {
            return Optional.ofNullable(this.mcloaders.get(uuid));
        }

        public void remove(UUID uuid) {
            this.mcloaders.remove(uuid);
        }

        public void close() throws Exception {
            this.mcloaders.clear();
        }
    }
}
