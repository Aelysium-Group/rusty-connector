package group.aelysium.rustyconnector.plugin.velocity.lib.database;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.database.MySQLService;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.HomeServerMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.central.Processor;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import static group.aelysium.rustyconnector.plugin.velocity.central.Processor.ValidServices.SERVER_SERVICE;
import static group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService.ValidServices.MYSQL_SERVICE;

public class HomeServerMappingsDatabase {
    private static final String FIND_HOME_SERVER_IN_FAMILY = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String CHECK_IF_PLAYER_HAS_HOME = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String DELETE_PLAYERS_HOME_SERVER = "DELETE FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String SAVE_PLAYERS_HOME_SERVER = "REPLACE INTO home_server_mappings (player_uuid, family_name, server_address, server_name, expiration) VALUES(?, ?, ?, ?, FROM_UNIXTIME(?));";
    private static final String PURGE_FAMILY_EXPIRED_MAPPINGS = "DELETE FROM home_server_mappings WHERE family_name = ? AND expiration < NOW();";
    private static final String UPDATE_NULL_EXPIRATIONS = "UPDATE home_server_mappings SET expiration = ? WHERE family_name = ? AND expiration IS NULL;";
    private static final String UPDATE_NOT_NULL_EXPIRATIONS = "UPDATE home_server_mappings SET expiration = NULL WHERE family_name = ? AND expiration IS NOT NULL;";

    /**
     * Initialize the table for home server mappings.
     */
    public static void init(MySQLService service) throws SQLException, IOException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        InputStream stream = api.getResourceAsStream("home_server_mappings.sql");
        String file = new String(stream.readAllBytes());

        service.connect();
        PreparedStatement statement = service.prepare(file);
        service.execute(statement);
        service.close();
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
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(FIND_HOME_SERVER_IN_FAMILY);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        ResultSet result = mySQLService.executeQuery(statement);

        if(!result.next()) return null;

        RegisteredServer registeredServer = api.getServer().getServer(result.getString("server_name")).orElse(null);
        if(registeredServer == null) return null;
        PlayerServer server = api.getService(SERVER_SERVICE).orElseThrow().findServer(registeredServer.getServerInfo());

        mySQLService.close();
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
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(CHECK_IF_PLAYER_HAS_HOME);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        ResultSet result = mySQLService.executeQuery(statement);
        mySQLService.close();

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
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(DELETE_PLAYERS_HOME_SERVER);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.getName());
        mySQLService.execute(statement);
        mySQLService.close();
    }

    /**
     * Save a home server mapping to the database.
     * If a mapping for the player already exists, this will overwrite it.
     * @param mapping The mapping to save.
     * @throws SQLException If there was an issue with the query.
     */
    public static void save(HomeServerMapping mapping) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        LiquidTimestamp expiration = mapping.family().getHomeServerExpiration();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(SAVE_PLAYERS_HOME_SERVER);
        statement.setString(1, mapping.player().getUniqueId().toString());
        statement.setString(2, mapping.family().getName());
        statement.setString(3, mapping.server().getAddress());
        statement.setString(4, mapping.server().getServerInfo().getName());
        if(expiration == null)
            statement.setNull(5, Types.NULL);
        else
            statement.setLong(5, expiration.getEpochFromNow());

        mySQLService.execute(statement);
        mySQLService.close();
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void purgeExpired(StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(PURGE_FAMILY_EXPIRED_MAPPINGS);
        statement.setString(1, family.getName());
        mySQLService.execute(statement);
        mySQLService.close();
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void updateNullExpirations(StaticServerFamily family) throws SQLException {
        LiquidTimestamp liquidExpiration = family.getHomeServerExpiration();
        if(liquidExpiration == null) return;

        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        Timestamp expiration = new Timestamp(liquidExpiration.getEpochFromNow());

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(UPDATE_NULL_EXPIRATIONS);
        statement.setTimestamp(1, expiration);
        statement.setString(2, family.getName());
        mySQLService.execute(statement);
        mySQLService.close();
    }

    /**
     * If any home servers are set to expire, and if an expiration time is disabled in the family,
     * This will update all expirations to now never expire;
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public static void updateValidExpirations(StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityRustyConnector.getAPI();
        MySQLService mySQLService = api.getService(MYSQL_SERVICE).orElseThrow();

        mySQLService.connect();
        PreparedStatement statement = mySQLService.prepare(UPDATE_NOT_NULL_EXPIRATIONS);
        statement.setString(1, family.getName());
        mySQLService.execute(statement);
        mySQLService.close();
    }
}
