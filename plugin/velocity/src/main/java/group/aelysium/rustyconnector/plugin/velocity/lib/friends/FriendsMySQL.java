package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLConnection;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLConnector;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FriendsMySQL {
    private static final String FIND_FRIENDS = "SELECT * FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String GET_FRIEND_COUNT = "SELECT COUNT(*) FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE player1_uuid = ? AND player2_uuid = ?;";
    private static final String ADD_FRIEND = "REPLACE INTO friends (player1_uuid, player2_uuid) VALUES(?, ?);";

    private final MySQLConnector connector;

    public FriendsMySQL(MySQLConnector connector) {
        this.connector = connector;
    }

    public void init() throws SQLException, IOException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        VelocityAPI api = VelocityAPI.get();
        InputStream stream = api.resourceAsStream("friends.sql");
        String file = new String(stream.readAllBytes());

        connection.connect();
        PreparedStatement statement = connection.prepare(file);
        connection.execute(statement);
        connection.close();
    }
    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public Optional<List<FriendMapping>> findFriends(Player player) {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        VelocityAPI api = VelocityAPI.get();
        PlayerService playerService = api.services().playerService().orElseThrow();

        try {
            connection.connect();
            PreparedStatement statement = connection.prepare(FIND_FRIENDS);
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getUniqueId().toString());

            ResultSet result = connection.executeQuery(statement);

            List<FriendMapping> friends = new ArrayList<>();
            while (result.next()) {
                FakePlayer player1 = playerService.findPlayer(UUID.fromString(result.getString("player1_uuid")));
                FakePlayer player2 = playerService.findPlayer(UUID.fromString(result.getString("player2_uuid")));

                if (player1 == null) continue;
                if (player2 == null) continue;

                friends.add(new FriendMapping(player1, player2));
            }

            connection.close();
            return Optional.of(friends);
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public boolean areFriends(FakePlayer player1, FakePlayer player2) {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        VelocityAPI api = VelocityAPI.get();
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        try {
            connection.connect();
            PreparedStatement statement = connection.prepare(FIND_FRIENDS);
            statement.setString(1, orderedMapping.player1().uuid().toString());
            statement.setString(2, orderedMapping.player2().uuid().toString());

            ResultSet result = connection.executeQuery(statement);
            if(!result.next()) return false;

            connection.close();
            return true;
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
        }

        return false;
    }

    public void addFriend(Player player1, Player player2) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        connection.connect();
        PreparedStatement statement = connection.prepare(ADD_FRIEND);
        statement.setString(1, orderedMapping.player1().uuid().toString());
        statement.setString(2, orderedMapping.player2().uuid().toString());

        connection.execute(statement);

        connection.close();
    }

    public void removeFriend(Player player1, Player player2) throws SQLException {
        MySQLConnection connection = this.connector.connection().orElseThrow();
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        connection.connect();
        PreparedStatement statement = connection.prepare(DELETE_FRIEND);
        statement.setString(1, orderedMapping.player1().uuid().toString());
        statement.setString(2, orderedMapping.player2().uuid().toString());

        connection.execute(statement);

        connection.close();
    }
}
