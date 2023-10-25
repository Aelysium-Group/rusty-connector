package group.aelysium.rustyconnector.core.lib.messenger.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.api.core.UserPass;
import io.lettuce.core.protocol.ProtocolVersion;

import java.io.File;
import java.net.InetSocketAddress;

public class ConnectorsConfig extends YAML {
    private InetSocketAddress redis_address;
    private UserPass redis_user;
    private ProtocolVersion redis_protocol;

    private String redis_dataChannel;

    private InetSocketAddress mysql_address;
    private UserPass mysql_user;
    private String mysql_database;

    public InetSocketAddress getRedis_address() {
        return redis_address;
    }

    public UserPass getRedis_user() {
        return redis_user;
    }

    public ProtocolVersion getRedis_protocol() {
        return redis_protocol;
    }

    public String getRedis_dataChannel() {
        return redis_dataChannel;
    }


    public InetSocketAddress getMysql_address() {
        return mysql_address;
    }

    public UserPass getMysql_user() {
        return mysql_user;
    }

    public String getMysql_database() {
        return mysql_database;
    }

    public ConnectorsConfig(File configPointer) {
        super(configPointer);
    }

    @SuppressWarnings("unchecked")
    public void register(boolean loadMessengers, boolean loadStorage) throws IllegalStateException {
        if(loadMessengers) {
            String host = this.getNode(this.data, "redis.host", String.class);
            if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
            int port = this.getNode(this.data, "redis.port", Integer.class);
            this.redis_address = new InetSocketAddress(host, port);
            String user = this.getNode(this.data, "redis.user", String.class);
            if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
            char[] password = this.getNode(this.data, "redis.password", String.class).toCharArray();
            this.redis_user = new UserPass(user, password);

            this.redis_protocol = ProtocolVersion.RESP2;
            try {
                this.redis_protocol = ProtocolVersion.valueOf(this.getNode(this.data, "redis.protocol", String.class));
            } catch (Exception ignore) {}

            this.redis_dataChannel = this.getNode(this.data, "redis.data-channel", String.class);
            if (this.redis_dataChannel.equals("")) throw new IllegalStateException("Please configure your connector settings. `dataChannel` cannot be empty for Redis connectors.");
        }

        if(loadStorage) {
            String host = this.getNode(this.data, "mariadb.host", String.class);
            if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
            int port = this.getNode(this.data, "mariadb.port", Integer.class);
            this.mysql_address = new InetSocketAddress(host, port);

            String user = this.getNode(this.data, "mariadb.user", String.class);
            if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
            char[] password = this.getNode(this.data, "mariadb.password", String.class).toCharArray();
            this.mysql_user = new UserPass(user, password);
            this.mysql_database = this.getNode(this.data, "mariadb.database", String.class);
        }
    }
}
