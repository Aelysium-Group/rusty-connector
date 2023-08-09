package group.aelysium.rustyconnector.plugin.velocity.lib.viewport.micro_services;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import com.velocitypowered.api.proxy.Player;
import group.aelysium.rustyconnector.core.lib.database.mysql.MySQLService;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendMapping;
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

public class ViewportMySQLService extends MySQLService {
    private ViewportMySQLService(DataSource dataSource) {
        super(dataSource);
    }

    public void init() throws SQLException, IOException {
        VelocityAPI api = VelocityAPI.get();
        InputStream stream = api.resourceAsStream("mysql/viewport.sql");
        String file = new String(stream.readAllBytes());

        this.connect();
        PreparedStatement statement = this.prepare(file);
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

        public ViewportMySQLService.Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public ViewportMySQLService.Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public ViewportMySQLService.Builder setDatabase(String database) {
            this.database = database;
            return this;
        }

        public ViewportMySQLService.Builder setUser(String user) {
            this.user = user;
            return this;
        }

        public ViewportMySQLService.Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public ViewportMySQLService build(){
            MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setServerName(this.host);
            dataSource.setPortNumber(this.port);

            if(this.database != null)
                dataSource.setDatabaseName(this.database);

            if(this.user != null)
                dataSource.setUser(this.user);

            if(this.password != null)
                dataSource.setPassword(this.password);

            return new ViewportMySQLService(dataSource);
        }

    }
}
