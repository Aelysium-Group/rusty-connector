package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.core.lib.database.redis.RedisClient;
import group.aelysium.rustyconnector.core.lib.database.redis.RedisService;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.model.IKLifecycle;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.RedisMessagerService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Processor extends IKLifecycle<ProcessorServiceHandler> {
    protected Processor(Map<Class<? extends Service>, Service> services) {
        super(new ProcessorServiceHandler(services));
    }

    @Override
    public void kill() {
        this.services().killAll();
    }

    public static Processor init(DefaultConfig config) throws IllegalAccessException {
        PaperAPI api = PaperAPI.get();
        PluginLogger logger = api.logger();
        Processor.Builder builder = new Processor.Builder();

        // Setup private key
        PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "private.key"));
        if(!privateKeyConfig.generate())
            throw new IllegalStateException("Unable to load or create private.key!");
        char[] privateKey = null;
        try {
            privateKey = privateKeyConfig.get();
        } catch (Exception ignore) {}
        if(privateKey == null) throw new IllegalAccessException("There was a fatal error while reading private.key!");


        logger.log("Preparing Redis...");

        // Setup Redis
        RedisClient.Builder redisClientBuilder = new RedisClient.Builder()
                .setHost(config.getRedis_host())
                .setPort(config.getRedis_port())
                .setUser(config.getRedis_user())
                .setDataChannel(config.getRedis_dataChannel());

        if(!config.getRedis_password().equals(""))
            redisClientBuilder.setPassword(config.getRedis_password());

        builder.addService(new RedisService(redisClientBuilder, privateKey));
        logger.log("Finished setting up redis");

        ServerInfoService serverInfoService = new ServerInfoService(
                config.getServer_name(),
                AddressUtil.parseAddress(config.getServer_address()),
                config.getServer_family(),
                config.getServer_playerCap_soft(),
                config.getServer_playerCap_hard(),
                config.getServer_weight()
        );
        builder.addService(serverInfoService);

        // Setup message tunnel
        builder.addService(new MessageCacheService(50));
        logger.log("Set message cache size to be: 50");

        builder.addService(new RedisMessagerService());

        builder.addService(new DynamicTeleportService());

        builder.addService(new MagicLinkService(3));

        return builder.build();
    }


    protected static class Builder {
        private final Map<Class<? extends Service>, Service> services = new HashMap<>();

        public Builder addService(Service service) {
            this.services.put(service.getClass(), service);
            return this;
        }

        public Processor build() {
            return new Processor(this.services);
        }
    }
}
