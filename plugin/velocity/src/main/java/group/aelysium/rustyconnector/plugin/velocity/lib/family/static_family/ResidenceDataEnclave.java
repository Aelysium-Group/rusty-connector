package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.connectors.storage.*;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Destroyable;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Destructive;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.SyncedResource;
import group.aelysium.rustyconnector.core.lib.connectors.storage.synced_resource.Unsynced;
import group.aelysium.rustyconnector.core.lib.model.DataEnclave;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerDataEnclave;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ResidenceDataEnclave extends DataEnclave<ResidenceDataEnclave.ServerResidence, Boolean> {
    private static final StorageQuery PURGE_FAMILY_EXPIRED_MAPPINGS = StorageQuery.create("DELETE FROM home_server_mappings WHERE family_name = ? AND expiration < NOW();");
    private static final StorageQuery UPDATE_NULL_EXPIRATIONS = StorageQuery.create("UPDATE home_server_mappings SET expiration = ? WHERE family_name = ? AND expiration IS NULL;");
    private static final StorageQuery UPDATE_NOT_NULL_EXPIRATIONS = StorageQuery.create("UPDATE home_server_mappings SET expiration = NULL WHERE family_name = ? AND expiration IS NOT NULL;");

    public ResidenceDataEnclave(StorageConnector<?> connector) throws Exception {
        super(connector, 100, LiquidTimestamp.from(20, TimeUnit.MINUTES));
        StorageConnection connection = connector.connection().orElseThrow();

        String mysql = "";
        try(InputStream stream = Tinder.get().resourceAsStream("mysql/residence.sql")) {
            mysql = new String(stream.readAllBytes());
        } catch (Exception ignore) {}

        StorageQuery initialize = StorageQuery.create(mysql);
        connection.query(initialize);
    }

    protected Stream<ServerResidence> filter(Predicate<ServerResidence> predicate) {
        return this.cache.asMap().keySet().stream().filter(predicate);
    }

    public Optional<ServerResidence> fetch(Player player, StaticServerFamily family) {
        // Check the local cache for a matching residence
        try {
            return this.filter(entry -> entry.player().equals(player) && entry.family().equals(family)).findFirst();
        } catch (Exception ignore) {}

        // Ask MySQL for the residence and then cache it.
        try {
            ServerResidence serverResidence = ServerResidence.fetch(this.connector, player, family).orElseThrow();
            this.cache.put(serverResidence, false);
            return Optional.of(serverResidence);
        } catch (NoSuchElementException ignore) {}
        catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }
    public void save(Player player, PlayerServer server, StaticServerFamily family) throws Exception {
        ServerResidence serverResidence = ServerResidence.from(player, server, family);
        serverResidence.sync(this.connector);

        this.cache.put(serverResidence, false);
    }
    public void delete(Player player, StaticServerFamily family) throws Exception {
        ServerResidence serverResidence = ServerResidence.fetch(this.connector, player, family).orElseThrow();
        serverResidence.delete();
    }
    public void updateExpirations(LiquidTimestamp expiration, StaticServerFamily family) throws Exception {
        if(expiration == null)
            updateValidExpirations(this.connector, family);
        else
            updateNullExpirations(this.connector, family);
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    protected static void purgeExpired(StorageConnector<?> connector, StaticServerFamily family) throws Exception {
        StorageConnection connection = connector.connection().orElseThrow();

        connection.query(PURGE_FAMILY_EXPIRED_MAPPINGS, family.name());
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    protected static void updateNullExpirations(StorageConnector<?> connector, StaticServerFamily family) throws Exception {
        StorageConnection connection = connector.connection().orElseThrow();
        LiquidTimestamp liquidExpiration = family.homeServerExpiration();
        if(liquidExpiration == null) return;

        Timestamp expiration = new Timestamp(liquidExpiration.epochFromNow());

        connection.query(UPDATE_NULL_EXPIRATIONS, expiration, family.name());
    }

    /**
     * If any home servers are set to expire, and if an expiration time is disabled in the family,
     * This will update all expirations to now never expire;
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    protected static void updateValidExpirations(StorageConnector<?> connector, StaticServerFamily family) throws Exception {
        StorageConnection connection = connector.connection().orElseThrow();

        connection.query(UPDATE_NOT_NULL_EXPIRATIONS, family.name());
    }

    protected static class ServerResidence extends SyncedResource {
        private static final StorageQuery FIND_HOME_SERVER_IN_FAMILY = StorageQuery.create("SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;");
        private static final StorageQuery CHECK_IF_PLAYER_HAS_HOME = StorageQuery.create("SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;");
        private static final StorageQuery DELETE_PLAYERS_HOME_SERVER = StorageQuery.create("DELETE FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;");
        private static final StorageQuery SAVE_PLAYERS_HOME_SERVER = StorageQuery.create("REPLACE INTO home_server_mappings (player_uuid, family_name, server_address, server_name, expiration) VALUES(?, ?, ?, ?, FROM_UNIXTIME(?));");

        protected Player player;
        protected PlayerServer server;
        protected StaticServerFamily family;
        protected ServerResidence(StorageConnector<?> connector, Player player, PlayerServer server, StaticServerFamily family) {
            super(connector);
            this.player = player;
            this.server = server;
            this.family = family;
        }
        protected ServerResidence(Player player, PlayerServer server, StaticServerFamily family) {
            this.player = player;
            this.server = server;
            this.family = family;
        }

        @Destroyable
        public Player player() {
            this.throwDestroyed();
            return this.player;
        }

        @Destroyable
        public PlayerServer server() {
            this.throwDestroyed();
            return this.server;
        }

        @Destroyable
        public StaticServerFamily family() {
            this.throwDestroyed();
            return this.family;
        }

        protected static Optional<ServerResidence> fetch(StorageConnector<?> connector, Player player, StaticServerFamily family) throws Exception {
            Tinder api = Tinder.get();

            StorageConnection connection = connector.connection().orElseThrow();

            AtomicReference<Optional<ServerResidence>> homeServerMapping = new AtomicReference<>(Optional.empty());
            try {
                Consumer<StorageResponse<?>> consumer = (result) -> {
                    ResultConsumer resultConsumer = result.consumer();

                    resultConsumer.forMySQL((mysqlResult) -> {
                        try {
                            RegisteredServer registeredServer = api.velocityServer().getServer(mysqlResult.first().getString("server_name")).orElse(null);
                            if(registeredServer == null) return;
                            PlayerServer server = api.services().serverService().search(registeredServer.getServerInfo());

                            homeServerMapping.set(Optional.of(new ServerResidence(player, server, family)));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    });

                    resultConsumer.trigger();
                };

                connection.query(FIND_HOME_SERVER_IN_FAMILY, consumer, player.getUniqueId().toString(), family.name());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return homeServerMapping.get();
        }

        public void sync(StorageConnector<?> connector) throws Exception {
            this.throwDestroyed();

            StorageConnection connection = connector.connection().orElseThrow();

            LiquidTimestamp expiration = family.homeServerExpiration();

            if(expiration == null)
                connection.query(
                        SAVE_PLAYERS_HOME_SERVER,
                        player.getUniqueId().toString(),
                        family.name(),
                        server.address(),
                        server.serverInfo().getName(),
                        null
                );
            else
                connection.query(
                        DELETE_PLAYERS_HOME_SERVER,
                        player.getUniqueId().toString(),
                        family.name(),
                        server.address(),
                        server.serverInfo().getName(),
                        expiration.epochFromNow()
                );

            this.connector = connector;
            this.synced = true;
        }

        @Unsynced
        public static ServerResidence from(Player player, PlayerServer server, StaticServerFamily family) {
            return new ServerResidence(player, server, family);
        }

        @Override
        @Destructive
        public void delete() throws Exception {
            StorageConnection<StorageResponse<?>> connection = connector.connection().orElseThrow();

            connection.query(DELETE_PLAYERS_HOME_SERVER, player.getUniqueId().toString(), family.name());

            this.destroyed = true;
        }
    }
}
