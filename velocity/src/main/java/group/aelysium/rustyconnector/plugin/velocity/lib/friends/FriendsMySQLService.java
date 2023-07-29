package group.aelysium.rustyconnector.plugin.velocity.lib.friends;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.PlayerService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FriendsMySQLService extends MySQLService {
    private static final String FIND_FRIENDS = "SELECT * FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String GET_FRIEND_COUNT = "SELECT COUNT(*) FROM friends WHERE player1_uuid = ? OR player2_uuid = ?;";
    private static final String DELETE_FRIEND = "DELETE FROM friends WHERE player1_uuid = ? AND player2_uuid = ?;";
    private static final String ADD_FRIEND = "REPLACE INTO friends (player1_uuid, player2_uuid) VALUES(?, ?);";

    private FriendsMySQLService(DataSource dataSource) {
        super(dataSource);
    }

    public void init() throws SQLException, IOException {
        VelocityAPI api = VelocityAPI.get();
        InputStream stream = api.resourceAsStream("friends.sql");
        String file = new String(stream.readAllBytes());

        this.connect();
        PreparedStatement statement = this.prepare(file);
        this.execute(statement);
        this.close();
    }
    /**
     * Find all friends of a player.
     * @param player The player to find friends of.
     * @return A list of friends.
     * @throws SQLException If there was an issue.
     */
    public Optional<List<FriendMapping>> findFriends(Player player) {
        VelocityAPI api = VelocityAPI.get();
        PlayerService playerService = api.services().playerService().orElseThrow();

        try {
            this.connect();
            PreparedStatement statement = this.prepare(FIND_FRIENDS);
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getUniqueId().toString());

            ResultSet result = this.executeQuery(statement);

            List<FriendMapping> friends = new ArrayList<>();
            while (result.next()) {
                FakePlayer player1 = playerService.findPlayer(UUID.fromString(result.getString("player1_uuid")));
                FakePlayer player2 = playerService.findPlayer(UUID.fromString(result.getString("player2_uuid")));

                if (player1 == null) continue;
                if (player2 == null) continue;

                friends.add(new FriendMapping(player1, player2));
            }

            this.close();
            return Optional.of(friends);
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public boolean areFriends(FakePlayer player1, FakePlayer player2) {
        VelocityAPI api = VelocityAPI.get();
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        try {
            this.connect();
            PreparedStatement statement = this.prepare(FIND_FRIENDS);
            statement.setString(1, orderedMapping.player1().uuid().toString());
            statement.setString(2, orderedMapping.player2().uuid().toString());

            ResultSet result = this.executeQuery(statement);
            if(!result.next()) return false;

            this.close();
            return true;
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
        }

        return false;
    }

    public void addFriend(Player player1, Player player2) throws SQLException {
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        this.connect();
        PreparedStatement statement = this.prepare(ADD_FRIEND);
        statement.setString(1, orderedMapping.player1().uuid().toString());
        statement.setString(2, orderedMapping.player2().uuid().toString());

        this.execute(statement);

        this.close();
    }

    public void removeFriend(Player player1, Player player2) throws SQLException {
        FriendMapping orderedMapping = new FriendMapping(player1, player2);

        this.connect();
        PreparedStatement statement = this.prepare(DELETE_FRIEND);
        statement.setString(1, orderedMapping.player1().uuid().toString());
        statement.setString(2, orderedMapping.player2().uuid().toString());

        this.execute(statement);

        this.close();
    }

    public static class Builder {
        protected String host;
        protected int port;

        protected String database;
        protected String user;
        protected String password;

        public Builder(){}

        public FriendsMySQLService.Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public FriendsMySQLService.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public FriendsMySQLService.Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public FriendsMySQLService.Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public FriendsMySQLService.Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public FriendsMySQLService build(){
            MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setServerName(this.host);
            dataSource.setPortNumber(this.port);

            if(this.database != null)
                dataSource.setDatabaseName(this.database);

            if(this.user != null)
                dataSource.setUser(this.user);

            if(this.password != null)
                dataSource.setPassword(this.password);

            return new FriendsMySQLService(dataSource);
        }

    }
}
