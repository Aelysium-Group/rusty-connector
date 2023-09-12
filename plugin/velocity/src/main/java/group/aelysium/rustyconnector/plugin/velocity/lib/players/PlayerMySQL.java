package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnection;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.storage.mysql.MySQLConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PlayerMySQL {
    private static final String FIND_PLAYER = "SELECT * FROM players WHERE uuid = ?;";
    private static final String FIND_PLAYER_USERNAME = "SELECT * FROM players WHERE username = ?;";
    private static final String ADD_PLAYER = "REPLACE INTO players (uuid, username) VALUES(?, ?);";

    private final MySQLConnector connector;

    public PlayerMySQL(MySQLConnector connector) {
        this.connector = connector;
    }

    public void init() throws SQLException, IOException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        Tinder api = Tinder.get();
        InputStream stream = api.resourceAsStream("players.sql");
        String file = new String(stream.readAllBytes());

        connection.connect();
        PreparedStatement statement = connection.prepare(file);
        connection.execute(statement);
        connection.close();
    }

    public Optional<FakePlayer> resolveUUID(UUID uuid) {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        Tinder api = Tinder.get();

        try {
            connection.connect();
            PreparedStatement statement = connection.prepare(FIND_PLAYER);
            statement.setString(1, uuid.toString());

            ResultSet result = connection.executeQuery(statement);
            if(!result.next()) return Optional.empty();

            String username = result.getString("username");

            connection.close();
            return Optional.of(new FakePlayer(uuid, username));
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public Optional<FakePlayer> resolveUsername(String username) {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        Tinder api = Tinder.get();

        try {
            connection.connect();
            PreparedStatement statement = connection.prepare(FIND_PLAYER_USERNAME);
            statement.setString(1, username);

            ResultSet result = connection.executeQuery(statement);
            if(!result.next()) return Optional.empty();

            UUID uuid = UUID.fromString(result.getString("uuid"));

            connection.close();
            return Optional.of(new FakePlayer(uuid, username));
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public void addPlayer(Player player) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        connection.connect();
        PreparedStatement statement = connection.prepare(ADD_PLAYER);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, player.getUsername());

        connection.execute(statement);

        connection.close();
    }
}
