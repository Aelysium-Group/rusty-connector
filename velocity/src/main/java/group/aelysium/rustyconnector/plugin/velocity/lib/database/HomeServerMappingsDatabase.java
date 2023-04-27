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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HomeServerMappingsDatabase {
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
        PreparedStatement statement = mySQL.prepare("SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?");
        statement.setString(0, player.getUniqueId().toString());
        statement.setString(1, family.getName());
        ResultSet result = mySQL.execute(statement);
        mySQL.close();

        if(!result.next()) return null;

        RegisteredServer registeredServer = api.getServer().getServer(result.getString("server_name")).orElse(null);
        if(registeredServer == null) return null;
        PlayerServer server = api.getVirtualProcessor().findServer(registeredServer.getServerInfo());

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
        PreparedStatement statement = mySQL.prepare("SELECT * FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?");
        statement.setString(0, player.getUniqueId().toString());
        statement.setString(1, family.getName());
        ResultSet result = mySQL.execute(statement);
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
        PreparedStatement statement = mySQL.prepare("DELETE FROM home_server_mappings WHERE player_uuid = ? AND family_name = ?");
        statement.setString(0, player.getUniqueId().toString());
        statement.setString(1, family.getName());
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

        mySQL.connect();
        PreparedStatement statement = mySQL.prepare(
                "INSERT INTO table (player_uuid, family_name, server_address, server_name, expiration)" +
                        "VALUES(?, ?, ?, ?, ?)" +
                        "ON DUPLICATE KEY UPDATE");
        statement.setString(0, mapping.player().getUniqueId().toString());
        statement.setString(1, mapping.family().getName());
        statement.setString(2, mapping.server().getAddress());
        statement.setString(3, mapping.server().getServerInfo().getName());
        statement.setString(4, String.valueOf(mapping.family().getHomeServerExpiration().getEpochFromNow()));
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
        PreparedStatement statement = mySQL.prepare("DELETE FROM home_server_mappings WHERE family_name = ? AND expiration < NOW()");
        statement.setString(0, family.getName());
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
        PreparedStatement statement = mySQL.prepare("UPDATE home_server_mappings SET expiration = ? WHERE family_name = ? AND expiration IS NULL;");
        statement.setDate(0, new Date(expiration.getEpochFromNow()));
        statement.setString(1, family.getName());
        mySQL.execute(statement);
        mySQL.close();
    }
}
