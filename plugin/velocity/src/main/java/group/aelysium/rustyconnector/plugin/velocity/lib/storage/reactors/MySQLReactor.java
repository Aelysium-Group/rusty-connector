package group.aelysium.rustyconnector.plugin.velocity.lib.storage.reactors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.Player;
import group.aelysium.rustyconnector.toolkit.core.UserPass;
import group.aelysium.rustyconnector.toolkit.velocity.family.static_family.IServerResidence;
import group.aelysium.rustyconnector.toolkit.velocity.friends.PlayerPair;
import group.aelysium.rustyconnector.toolkit.core.matchmaking.IPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IRankResolver;
import group.aelysium.rustyconnector.toolkit.velocity.matchmaking.IVelocityPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;

import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MySQLReactor extends StorageReactor {
    protected static String PLAYERS_TABLE =
            "CREATE TABLE IF NOT EXISTS players (" +
                    "    uuid VARCHAR(36) NOT NULL," +
                    "    username VARCHAR(16) NOT NULL," +
                    "    PRIMARY KEY (uuid)" +
                    ");";
    protected static String SERVER_RESIDENCES_TABLE =
            "CREATE TABLE IF NOT EXISTS server_residences (" +
                    "    player_uuid VARCHAR(36) NOT NULL," +
                    "    family_id VARCHAR(16) NOT NULL," +
                    "    mcloader_uuid VARCHAR(36) NOT NULL," +
                    "    expiration TIMESTAMP NULL DEFAULT NULL," +
                    "    FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
                    "    CONSTRAINT uc_Mappings UNIQUE (player_uuid, family_id)" +
                    ");";
    protected static String FRIEND_LINKS_TABLE =
            "CREATE TABLE IF NOT EXISTS friend_links (" +
                    "    player1_uuid VARCHAR(36) NOT NULL," +
                    "    player2_uuid VARCHAR(36) NOT NULL," +
                    "    FOREIGN KEY (player1_uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
                    "    FOREIGN KEY (player2_uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
                    "    CONSTRAINT uc_Mappings UNIQUE (player1_uuid, player2_uuid)" +
                    ");";
    protected static String PLAYER_RANKS_TABLE =
            "CREATE TABLE IF NOT EXISTS player_ranks (" +
                    "    player_uuid VARCHAR(36) NOT NULL," +
                    "    game_id VARCHAR(16) NOT NULL," +
                    "    schema VARCHAR(32) NOT NULL," +
                    "    rank VARCHAR(256) NOT NULL," +
                    "    FOREIGN KEY (player_uuid) REFERENCES players(uuid) ON DELETE CASCADE," +
                    "    CONSTRAINT uc_Mappings UNIQUE (player_uuid, game_id)" +
                    ");";

    protected final Core core;

    public MySQLReactor(Core.Settings settings) {
        this.core = new Core(settings);
        this.initializeDatabase();
    }

    @Override
    public void initializeDatabase() {
        try {
            this.core.execute(this.core.prepare(PLAYERS_TABLE));
            this.core.execute(this.core.prepare(SERVER_RESIDENCES_TABLE));
            this.core.execute(this.core.prepare(FRIEND_LINKS_TABLE));
            this.core.execute(this.core.prepare(PLAYER_RANKS_TABLE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateExpirations(String familyId, Long newExpirationEpoch) {
        if(newExpirationEpoch == null)
            try {
                PreparedStatement statement = this.core.prepare("UPDATE server_residences SET expiration = NULL WHERE family_id = ?;");
                statement.setString(1, familyId);
                this.core.execute(statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            try {
                PreparedStatement statement = this.core.prepare("UPDATE server_residences SET expiration = FROM_UNIXTIME(?) WHERE family_id = ? AND expiration IS NULL;");
                  statement.setLong(1, newExpirationEpoch);
                statement.setString(2, familyId);
                this.core.execute(statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void purgeExpiredServerResidences() {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM server_residences WHERE expiration < NOW();");
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<IServerResidence.MCLoaderEntry> fetchServerResidence(String familyId, UUID player) {
        return Optional.empty();
    }

    @Override
    public void deleteServerResidence(String familyId, UUID player) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM server_residences WHERE family_id = ? AND player_uuid = ?;");
            statement.setString(1, familyId);
            statement.setString(2, player.toString());
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteServerResidences(String familyId) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM server_residences WHERE family_id = ?;");
            statement.setString(1, familyId);
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveServerResidence(String familyId, UUID mcloader, UUID player, Long expirationEpoch) {
        if(expirationEpoch == null)
            try {
                PreparedStatement statement = this.core.prepare("REPLACE INTO server_residences (player_uuid, family_id, mcloader_uuid, expiration) VALUES(?, ?, ?, NULL);");

                statement.setString(1, player.toString());
                statement.setString(2, familyId);
                statement.setString(3, mcloader.toString());
                this.core.execute(statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        else
            try {
                PreparedStatement statement = this.core.prepare("REPLACE INTO server_residences (player_uuid, family_id, mcloader_uuid, expiration) VALUES(?, ?, ?, FROM_UNIXTIME(?));");

                statement.setString(1, player.toString());
                statement.setString(2, familyId);
                statement.setString(3, mcloader.toString());
                  statement.setLong(4, expirationEpoch);
                this.core.execute(statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    @Override
    public void deleteFriendLink(PlayerPair pair) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM friend_links WHERE player1_uuid = ? AND player2_uuid = ?;");
            statement.setString(1, pair.player1().uuid().toString());
            statement.setString(2, pair.player2().uuid().toString());
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<Boolean> areFriends(PlayerPair pair) {
        try {
            PreparedStatement statement = this.core.prepare("SELECT * FROM friend_links WHERE player1_uuid = ? AND player2_uuid = ? LIMIT 1;");
            statement.setString(1, pair.player1().uuid().toString());
            statement.setString(2, pair.player2().uuid().toString());
            ResultSet result = this.core.executeQuery(statement);

            boolean hasRows = result.next();
            if(!hasRows) return Optional.of(false);
            return Optional.of(true);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<IPlayer>> fetchFriends(UUID player) {
        try {
            PreparedStatement statement = this.core.prepare(
                    "SELECT\n" +
                            "    IF(f.player1_uuid = ?, f.player2_uuid, f.player1_uuid) AS friend_uuid,\n" +
                            "    p.username AS friend_username\n" +
                            "FROM friend_links f\n" +
                            "JOIN players p ON p.uuid = IF(f.player1_uuid = ?, f.player2_uuid, f.player1_uuid)\n" +
                            "WHERE f.player1_uuid = ? OR f.player2_uuid = ?;"
            );
            String uuidAsString = player.toString();
            statement.setString(1, uuidAsString);
            statement.setString(2, uuidAsString);
            statement.setString(3, uuidAsString);
            statement.setString(4, uuidAsString);
            ResultSet result = this.core.executeQuery(statement);

            List<IPlayer> friends = new ArrayList<>();

            while(result.next()) {
                UUID uuid = UUID.fromString(result.getString("friend_uuid"));
                String username = result.getString("friend_username");

                friends.add(new Player(uuid, username));
            }

            return Optional.of(friends);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void saveFriendLink(PlayerPair pair) {
        try {
            PreparedStatement statement = this.core.prepare("REPLACE INTO friend_links (player1_uuid, player2_uuid) VALUES(?, ?);");

            statement.setString(1, pair.player1().uuid().toString());
            statement.setString(2, pair.player2().uuid().toString());
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<IPlayer> fetchPlayer(UUID uuid) {
        try {
            PreparedStatement statement = this.core.prepare("SELECT * FROM players WHERE uuid = ? LIMIT 1;");
            statement.setString(1, uuid.toString());
            ResultSet result = this.core.executeQuery(statement);

            boolean hasRows = result.next();
            if(!hasRows) return Optional.empty();

            String username = result.getString("username");

            return Optional.of(new Player(uuid, username));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<IPlayer> fetchPlayer(String username) {
        try {
            PreparedStatement statement = this.core.prepare("SELECT * FROM players WHERE username = ? LIMIT 1;");
            statement.setString(1, username);
            ResultSet result = this.core.executeQuery(statement);

            boolean hasRows = result.next();
            if(!hasRows) return Optional.empty();

            UUID uuid = UUID.fromString(result.getString("uuid"));

            return Optional.of(new Player(uuid, username));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void savePlayer(UUID uuid, String username) {
        try {
            PreparedStatement statement = this.core.prepare("INSERT IGNORE players (uuid, username) VALUES(?, ?);");
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGame(String gameId) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM player_ranks WHERE game_id = ?;");
            statement.setString(1, gameId);
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRank(UUID player, String gameId) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM player_ranks WHERE player_uuid = ? AND game_id = ?;");
            statement.setString(1, player.toString());
            statement.setString(2, gameId);
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteRank(UUID player) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM player_ranks WHERE player_uuid = ?;");
            statement.setString(1, player.toString());
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveRank(UUID player, String gameId, JsonObject rank) {
        try {
            PreparedStatement statement = this.core.prepare("REPLACE INTO player_ranks (player_uuid, game_id, schema, rank) VALUES(?, ?, ?, ?);");
            statement.setString(1, player.toString());
            statement.setString(2, gameId);
            statement.setString(3, rank.get("schema").getAsString());
            statement.setString(4, rank.toString());
        this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void purgeInvalidSchemas(String gameId, String validSchema) {
        try {
            PreparedStatement statement = this.core.prepare("DELETE FROM player_ranks WHERE game_id = ? AND schema <> ?;");
            statement.setString(1, gameId);
            statement.setString(2, validSchema);
            this.core.execute(statement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<IVelocityPlayerRank> fetchRank(UUID player, String gameId, IRankResolver resolver) {
        try {
            PreparedStatement statement = this.core.prepare("SELECT * FROM player_ranks WHERE player_uuid = ? AND game_id = ? LIMIT 1;");
            statement.setString(1, player.toString());
            statement.setString(2, gameId);
            ResultSet result = this.core.executeQuery(statement);

            boolean hasRows = result.next();
            if(!hasRows) return Optional.empty();

            Gson gson = new Gson();
            JsonObject rank = gson.fromJson(result.getString("rank"), JsonObject.class);
            System.out.println(rank);

            return Optional.of((IVelocityPlayerRank) resolver.resolve(rank));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public void kill() {
        this.core.kill();
    }

    public static class Core {
        private final MysqlDataSource dataSource;
        private final Settings settings;
        private Connection connection;

        private Core(Settings settings) {
            this.settings = settings;

            this.dataSource = new MysqlConnectionPoolDataSource();
            this.dataSource.setServerName(settings.address().getHostName());
            this.dataSource.setPortNumber(settings.address().getPort());
            this.dataSource.setUser(settings.userPass().user());
            this.dataSource.setPassword(new String(settings.userPass().password()));
            this.dataSource.setDatabaseName(settings.database());
        }

        /**
         * Gets the connection to the MySQL server.
         */
        public Connection connect() throws SQLException {
            Connection connection = this.dataSource.getConnection();

            if(connection.isValid(5000)) this.connection = connection;
            if(this.connection == null) throw new SQLException("The MySQL connection is invalid! No further information.");

            return this.connection;
        }

        /**
         * Closes the connection to the MySQL server.
         */
        public void kill() {
            if(this.connection == null) return;
            try {
                this.connection.commit();
            } catch (Exception ignore) {} // If autocommit=true this throws an exception. Just ignore it.
            try {
                this.connection.close();
            } catch (Exception ignore) {} // If autocommit=true this throws an exception. Just ignore it.
        }

        public PreparedStatement prepare(String statement) throws SQLException {
            return this.connect().prepareStatement(statement);
        }

        public boolean execute(PreparedStatement statement) throws SQLException {
            return statement.execute();
        }

        public ResultSet executeQuery(PreparedStatement statement) throws SQLException {
            return statement.executeQuery();
        }

        public record Settings(InetSocketAddress address, UserPass userPass, String database) {}
    }
}
