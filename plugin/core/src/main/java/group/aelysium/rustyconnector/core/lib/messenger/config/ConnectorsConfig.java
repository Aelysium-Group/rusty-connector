package group.aelysium.rustyconnector.core.lib.messenger.config;

import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.core.UserPass;
import group.aelysium.rustyconnector.toolkit.core.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.core.config.IYAML;
import group.aelysium.rustyconnector.toolkit.core.lang.LangFileMappings;
import io.lettuce.core.protocol.ProtocolVersion;

import java.net.InetSocketAddress;
import java.nio.file.Path;

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

    public String getRedis_protocol() {
        return redis_protocol.toString();
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

    protected ConnectorsConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_CONNECTORS_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    protected void register(boolean loadMessengers, boolean loadStorage) throws IllegalStateException {
        if(loadMessengers) {
            String host = IYAML.getValue(this.data, "redis.host", String.class);
            if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
            int port = IYAML.getValue(this.data, "redis.port", Integer.class);
            this.redis_address = new InetSocketAddress(host, port);
            String user = IYAML.getValue(this.data, "redis.user", String.class);
            if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
            char[] password = IYAML.getValue(this.data, "redis.password", String.class).toCharArray();
            this.redis_user = new UserPass(user, password);

            this.redis_protocol = ProtocolVersion.RESP2;
            try {
                this.redis_protocol = ProtocolVersion.valueOf(IYAML.getValue(this.data, "redis.protocol", String.class));
            } catch (Exception ignore) {}

            this.redis_dataChannel = IYAML.getValue(this.data, "redis.data-channel", String.class);
            if (this.redis_dataChannel.equals("")) throw new IllegalStateException("Please configure your connector settings. `dataChannel` cannot be empty for Redis connectors.");
        }

        if(loadStorage) {
            String host = IYAML.getValue(this.data, "mariadb.host", String.class);
            if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
            int port = IYAML.getValue(this.data, "mariadb.port", Integer.class);
            this.mysql_address = new InetSocketAddress(host, port);

            String user = IYAML.getValue(this.data, "mariadb.user", String.class);
            if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
            char[] password = IYAML.getValue(this.data, "mariadb.password", String.class).toCharArray();
            this.mysql_user = new UserPass(user, password);
            this.mysql_database = IYAML.getValue(this.data, "mariadb.database", String.class);
        }
    }

    public static ConnectorsConfig construct(Path dataFolder, LangService lang, boolean loadMessengers, boolean loadStorage) {
        ConnectorsConfig config = new ConnectorsConfig(dataFolder, "connectors.yml", "connectors", lang);
        config.register(loadMessengers, loadStorage);
        return config;
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(ConnectorsConfig.class);
    }
}
