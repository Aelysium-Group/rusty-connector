package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

public class ServerResidence {
    protected Player player;
    protected PlayerServer server;

    protected StaticServerFamily family;
    protected Long expiration;

    public ServerResidence(Player player, PlayerServer server, StaticServerFamily family, LiquidTimestamp expiration) {
        this.player = player;
        this.server = server;
        this.family = family;
        if(expiration == null) this.expiration = null;
        else this.expiration = expiration.epochFromNow();
    }

    public Player player() {
        return player;
    }

    public PlayerServer server() {
        return server;
    }

    public StaticServerFamily family() {
        return family;
    }

    public Long expiration() {
        return expiration;
    }

    public void expiration(LiquidTimestamp expiration) {
        if(expiration == null) this.expiration = null;
        else this.expiration = expiration.epochFromNow();
    }
}

/*

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
 */