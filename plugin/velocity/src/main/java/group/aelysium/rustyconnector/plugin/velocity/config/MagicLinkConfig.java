package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.lang.LangService;
import group.aelysium.rustyconnector.plugin.velocity.lib.magic_link.MagicLink;
import group.aelysium.rustyconnector.plugin.velocity.lib.remote_storage.Storage;
import group.aelysium.rustyconnector.toolkit.common.UserPass;
import group.aelysium.rustyconnector.toolkit.common.config.IConfigService;
import group.aelysium.rustyconnector.toolkit.common.lang.IConfig;
import group.aelysium.rustyconnector.toolkit.common.lang.LangFileMappings;

import java.net.InetSocketAddress;
import java.nio.file.Path;

public class MagicLinkConfig extends Config {
    private MagicLink.Configuration configuration;
    public MagicLink.Configuration storageConfiguration() {
        return this.configuration;
    }



    protected MagicLinkConfig(Path dataFolder, String target, String name, LangService lang) {
        super(dataFolder, target, name, lang, LangFileMappings.PROXY_CONNECTORS_TEMPLATE);
    }

    @SuppressWarnings("unchecked")
    protected void register() throws IllegalStateException {
        Storage.StorageType storageType = Storage.StorageType.valueOf(IConfig.getValue(this.data, "storage.provider", String.class));
        switch (storageType) {
            case MYSQL -> {
                String host = IConfig.getValue(this.data, "redis.host", String.class);
                if (host.equals(""))
                    throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
                int port = IConfig.getValue(this.data, "redis.port", Integer.class);
                this.redis_address = new InetSocketAddress(host, port);
                String user = IConfig.getValue(this.data, "redis.user", String.class);
                if (user.equals(""))
                    throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
                char[] password = IConfig.getValue(this.data, "redis.password", String.class).toCharArray();
                this.redis_user = new UserPass(user, password);

                this.redis_protocol = ProtocolVersion.RESP2;
                try {
                    this.redis_protocol = ProtocolVersion.valueOf(IConfig.getValue(this.data, "redis.protocol", String.class));
                } catch (Exception ignore) {
                }

                this.redis_dataChannel = IConfig.getValue(this.data, "redis.data-channel", String.class);
                if (this.redis_dataChannel.equals(""))
                    throw new IllegalStateException("Please configure your connector settings. `dataChannel` cannot be empty for Redis connectors.");

                this.configuration = new Storage.Configuration.MySQL(address, userPass, database);
            }
            default -> throw new NullPointerException("No proper Storage System was defined!");
        }
    }

    public static MagicLinkConfig construct(Path dataFolder, LangService lang) {
        MagicLinkConfig config = new MagicLinkConfig(dataFolder, "magic_link.yml", "magic_link", lang);
        config.register();
        return config;
    }

    @Override
    public IConfigService.ConfigKey key() {
        return IConfigService.ConfigKey.singleton(MagicLinkConfig.class);
    }
}
