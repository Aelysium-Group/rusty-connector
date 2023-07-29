package group.aelysium.rustyconnector.plugin.velocity.lib.players;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging.VelocityLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PlayerMySQLService extends MySQLService {
    private static final String FIND_PLAYER = "SELECT * FROM players WHERE uuid = ?;";
    private static final String ADD_PLAYER = "REPLACE INTO players (uuid, username) VALUES(?, ?);";

    private PlayerMySQLService(DataSource dataSource) {
        super(dataSource);
    }

    public void init() throws SQLException, IOException {
        VelocityAPI api = VelocityAPI.get();
        InputStream stream = api.resourceAsStream("players.sql");
        String file = new String(stream.readAllBytes());

        this.connect();
        PreparedStatement statement = this.prepare(file);
        this.execute(statement);
        this.close();
    }

    public Optional<FakePlayer> resolveUUID(UUID uuid) {
        VelocityAPI api = VelocityAPI.get();

        try {
            this.connect();
            PreparedStatement statement = this.prepare(FIND_PLAYER);
            statement.setString(1, uuid.toString());

            ResultSet result = this.executeQuery(statement);
            if(!result.next()) return Optional.empty();

            String username = result.getString("username");

            this.close();
            return Optional.of(new FakePlayer(uuid, username));
        } catch (Exception e) {
            api.logger().send(VelocityLang.BOXED_MESSAGE_COLORED.build(Component.text(e.getMessage()), NamedTextColor.RED));
        }

        return Optional.empty();
    }

    public void addPlayer(Player player) throws SQLException {
        this.connect();
        PreparedStatement statement = this.prepare(ADD_PLAYER);
        statement.setString(1, player.getUniqueId().toString());
        statement.setString(2, player.getUsername());

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

        public PlayerMySQLService.Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public PlayerMySQLService.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public PlayerMySQLService.Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public PlayerMySQLService.Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public PlayerMySQLService.Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public PlayerMySQLService build(){
            MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setServerName(this.host);
            dataSource.setPortNumber(this.port);

            if(this.database != null)
                dataSource.setDatabaseName(this.database);

            if(this.user != null)
                dataSource.setUser(this.user);

            if(this.password != null)
                dataSource.setPassword(this.password);

            return new PlayerMySQLService(dataSource);
        }

    }
}
