package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.database.MySQL;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.HomeServerMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PlayerServer;

import java.sql.*;

public class HomeServerMappingsDatabase {
    private static final String FIND_HOME_SERVER_IN_FAMILY = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String CHECK_IF_PLAYER_HAS_HOME = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String DELETE_PLAYERS_HOME_SERVER = "DELETE FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String SAVE_PLAYERS_HOME_SERVER = "REPLACE INTO home_server_mappings (player_uuid, family_name, server_address, server_name, expiration) VALUES(?, ?, ?, ?, FROM_UNIXTIME(?));";
    private static final String PURGE_FAMILY_OLD_SERVERS = "DELETE FROM home_server_mappings WHERE family_name = ? AND expiration < NOW();";
    private static final String UPDATE_NULL_EXPIRATIONS = "UPDATE home_server_mappings SET expiration = ? WHERE family_name = ? AND expiration IS NULL;";
    private static final String INIT_TABLE = "" +
            "CREATE TABLE IF NOT EXISTS home_server_mappings (" +
            "    player_uuid VARCHAR(36) NOT NULL," +
            "    family_name VARCHAR(32) NOT NULL," +
            "    server_address VARCHAR(128) NOT NULL," +
            "    server_name VARCHAR(128) NOT NULL," +
            "    expiration TIMESTAMP NULL DEFAULT NULL," +
            "    CONSTRAINT uc_Mappings UNIQUE (player_uuid, family_name)" +
            ");";

    /**
     * Initialize the table for home server mappings.
     */
    public static void init() throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(INIT_TABLE);
        mySQL.execute(statement);
        mySQL.close();
    }

    /**
     * Finds a home server mapping for a player.
     * @param player The player to search for.
     * @param family The family to search in.
     * @return A home server mapping or `null` if there isn't one.
     * @throws SQLException If there was an issue with the query.
     */
    public static HomeServerMapping find(Player player, StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(FIND_HOME_SERVER_IN_FAMILY);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        ResultSet result = mySQL.executeQuery(statement);

        if(!result.next()) return null;

        RegisteredServer registeredServer = api.getServer().getServer(result.getString("server_name")).orElse(null);
        if(registeredServer == null) return null;
        PlayerServer server = api.getVirtualProcessor().findServer(registeredServer.getServerInfo());

        mySQL.close();
        return new HomeServerMapping(player, server, family);
    }

    /**
     * Finds a home server mapping for a player.
     * @param player The player to search for.
     * @param family The family to search in.
     * @return `true` if an entry with the player in the family is found. `false` otherwise.
     * @throws SQLException If there was an issue with the query.
     */
    public static boolean doesPlayerHaveHome(Player player, StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(CHECK_IF_PLAYER_HAS_HOME);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        ResultSet result = mySQL.executeQuery(statement);
        mySQL.close();

        return result.next();
    }

    /**
     * Deletes a home server mapping from the database.
     * @param player The player to search for.
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void delete(Player player, StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(DELETE_PLAYERS_HOME_SERVER);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        mySQL.execute(statement);
        mySQL.close();
    }

    /**
     * Save a home server mapping to the database.
     * If a mapping for the player already exists, this will overwrite it.
     * @param mapping The mapping to save.
     * @throws SQLException If there was an issue with the query.
     */
    public static void save(HomeServerMapping mapping) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        LiquidTimestamp expiration = mapping.family().getHomeServerExpiration();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(SAVE_PLAYERS_HOME_SERVER);
        statement.setString(1, mapping.player().getUniqueId().toString());
        statement.setString(2, mapping.family().getName());
        statement.setString(3, mapping.server().getAddress());
        statement.setString(4, mapping.server().getServerInfo().getName());
        if(expiration == null)
            statement.setNull(5, Types.NULL);
        else
            statement.setLong(5, expiration.getEpochFromNow());

        mySQL.execute(statement);
        mySQL.close();
    }

    /**
     * Deletes all home server mappings for a family that are expired
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void purge(StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(PURGE_FAMILY_OLD_SERVERS);
        statement.setString(1, family.getName());
        mySQL.execute(statement);
        mySQL.close();
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void updateNulls(StaticServerFamily family) throws SQLException {
        LiquidTimestamp expiration = family.getHomeServerExpiration();
        if(expiration == null) return;

        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQL mySQL = api.getMySQL();

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(UPDATE_NULL_EXPIRATIONS);
        statement.setDate(1, new Date(expiration.getEpochFromNow()));
        statement.setString(2, family.getName());
        mySQL.execute(statement);
        mySQL.close();
    }
}
