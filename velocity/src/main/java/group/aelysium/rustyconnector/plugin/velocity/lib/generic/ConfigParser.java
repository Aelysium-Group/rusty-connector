package group.aelysium.rustyconnector.plugin.velocity.lib.generic;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import rustyconnector.RustyConnector;
import rustyconnector.generic.database.Redis;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class ConfigParser {
    public static void parseGeneric(Config config, RustyConnector plugin) {
        ConfigurationNode configData = config.getData();
        plugin.logger().log("-------| Configuring Proxy...");
        ConfigParser.parseFamilies(config, plugin);

        plugin.logger().log("---------| Preparing Redis...");
        rustyconnector.generic.database.Redis redis = new Redis();
        redis.setConnection(
                configData.getNode("redis.host").getString(),
                configData.getNode("redis.port").getInt(),
                configData.getNode("redis.password").getString(),
                configData.getNode("redis.data-channel").getString()
        );
        redis.connect();
        plugin.logger().log("---------| Finished!");


        plugin.logger().log("---------| Preparing Families...");
        if(configData.getNode("use-whitelist").getBoolean()) {

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a whitelist.");
        }

    }

    public static void parseFamilies(Config config, RustyConnector plugin) {
        ConfigurationNode configData = config.getData();
        plugin.logger().log("---------| Preparing Families...");


        List<String> familyNames = (List<String>) configData.getNode("families").getValue();
        familyNames.forEach(name -> {
            Config family = new Config(this, new File(this.getDataFolder(), "config.yml"), "config_whitelist.yml");
        });
    }

    public static void parseWhitelist(Config config, RustyConnector plugin) {

    }
}
