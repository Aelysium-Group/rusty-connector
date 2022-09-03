package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Redis;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.generic.lib.generic.parsing.YAML;

public class GenericParser {
    public static void parse(Config config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        ConfigurationNode configData = config.getData();
        plugin.logger().log("-------| Configuring Proxy...");
        FamilyParser.parse(config, plugin);

        plugin.logger().log("---------| Preparing Redis...");
        Redis redis = new Redis();
        plugin.setRedis(redis);

        redis.setConnection(
                YAML.get(configData,"redis.host").getString(),
                YAML.get(configData,"redis.port").getInt(),
                YAML.get(configData,"redis.password").getString(),
                YAML.get(configData,"redis.data-channel").getString()
        );
        redis.connect(plugin);
        plugin.logger().log("---------| Finished!");


        plugin.logger().log("---------| Preparing Families...");
        if(configData.getNode("use-whitelist").getBoolean()) {

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a whitelist.");
        }

    }


}
