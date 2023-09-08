package group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnection;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.HomeServerMapping;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public class StaticFamilyMySQL {
    private static final String FIND_HOME_SERVER_IN_FAMILY = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String CHECK_IF_PLAYER_HAS_HOME = "SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String DELETE_PLAYERS_HOME_SERVER = "DELETE FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?;";
    private static final String SAVE_PLAYERS_HOME_SERVER = "REPLACE INTO home_server_mappings (player_uuid, family_name, server_address, server_name, expiration) VALUES(?, ?, ?, ?, FROM_UNIXTIME(?));";
    private static final String PURGE_FAMILY_EXPIRED_MAPPINGS = "DELETE FROM home_server_mappings WHERE family_name = ? AND expiration < NOW();";
    private static final String UPDATE_NULL_EXPIRATIONS = "UPDATE home_server_mappings SET expiration = ? WHERE family_name = ? AND expiration IS NULL;";
    private static final String UPDATE_NOT_NULL_EXPIRATIONS = "UPDATE home_server_mappings SET expiration = NULL WHERE family_name = ? AND expiration IS NOT NULL;";

    private final MySQLConnector connector;

    public StaticFamilyMySQL(MySQLConnector connector) {
        this.connector = connector;
    }

    /**
     * Initialize the table for home server mappings.
     */
    public void init() throws SQLException, IOException {
        VelocityAPI api = VelocityAPI.get();
        InputStream stream = api.resourceAsStream("home_server_mappings.sql");
        String file = new String(stream.readAllBytes());

        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(file);
        connection.execute(statement);
        connection.close();
    }

    /**
     * Finds a home server mapping for a player.
     * @param player The player to search for.
     * @param family The family to search in.
     * @return A home server mapping or `null` if there isn't one.
     * @throws SQLException If there was an issue with the query.
     */
    public HomeServerMapping find(Player player, StaticServerFamily family) throws SQLException {
        VelocityAPI api = VelocityAPI.get();

        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(FIND_HOME_SERVER_IN_FAMILY);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.name());
        ResultSet result = connection.executeQuery(statement);

        if(!result.next()) return null;

        RegisteredServer registeredServer = api.velocityServer().getServer(result.getString("server_name")).orElse(null);
        if(registeredServer == null) return null;
        PlayerServer server = api.services().serverService().search(registeredServer.getServerInfo());

        connection.close();
        return new HomeServerMapping(player, server, family);
    }

    /**
     * Finds a home server mapping for a player.
     * @param player The player to search for.
     * @param family The family to search in.
     * @return `true` if an entry with the player in the family is found. `false` otherwise.
     * @throws SQLException If there was an issue with the query.
     */
    public boolean doesPlayerHaveHome(Player player, StaticServerFamily family) throws SQLException {

        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(CHECK_IF_PLAYER_HAS_HOME);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.name());
        ResultSet result = connection.executeQuery(statement);
        connection.close();

        return result.next();
    }

    /**
     * Deletes a home server mapping from the database.
     * @param player The player to search for.
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public void delete(Player player, StaticServerFamily family) throws SQLException {

        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(DELETE_PLAYERS_HOME_SERVER);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, family.name());
        connection.execute(statement);
        connection.close();
    }

    /**
     * Save a home server mapping to the database.
     * If a mapping for the player already exists, this will overwrite it.
     * @param mapping The mapping to save.
     * @throws SQLException If there was an issue with the query.
     */
    public void save(HomeServerMapping mapping) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();

        LiquidTimestamp expiration = mapping.family().homeServerExpiration();

        connection.connect();
        PreparedStatement statement = connection.prepare(SAVE_PLAYERS_HOME_SERVER);
        statement.setString(1, mapping.player().getUniqueId().toString());
        statement.setString(2, mapping.family().name());
        statement.setString(3, mapping.server().address());
        statement.setString(4, mapping.server().serverInfo().getName());
        if(expiration == null)
            statement.setNull(5, Types.NULL);
        else
            statement.setLong(5, expiration.epochFromNow());

        connection.execute(statement);
        connection.close();
    }

    /**
     * Deletes all mappings that are expired.
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public void purgeExpired(StaticServerFamily family) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(PURGE_FAMILY_EXPIRED_MAPPINGS);
        statement.setString(1, family.name());
        connection.execute(statement);
        connection.close();
    }

    /**
     * If any home servers are set to never expire, and if an expiration time is set in the family,
     * This will update all null expirations to now expire at delay + NOW();
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public void updateNullExpirations(StaticServerFamily family) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        LiquidTimestamp liquidExpiration = family.homeServerExpiration();
        if(liquidExpiration == null) return;

        Timestamp expiration = new Timestamp(liquidExpiration.epochFromNow());

        connection.connect();
        PreparedStatement statement = connection.prepare(UPDATE_NULL_EXPIRATIONS);
        statement.setTimestamp(1, expiration);
        statement.setString(2, family.name());
        connection.execute(statement);
        connection.close();
    }

    /**
     * If any home servers are set to expire, and if an expiration time is disabled in the family,
     * This will update all expirations to now never expire;
     * @param family The family to search in.
     * @throws SQLException If there was an issue with the query.
     */
    public void updateValidExpirations(StaticServerFamily family) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();

        connection.connect();
        PreparedStatement statement = connection.prepare(UPDATE_NOT_NULL_EXPIRATIONS);
        statement.setString(1, family.name());
        connection.execute(statement);
        connection.close();
    }
}
