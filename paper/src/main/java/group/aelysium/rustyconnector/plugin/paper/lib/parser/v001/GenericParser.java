package group.aelysium.rustyconnector.plugin.paper.lib.parser.v001;

import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.parsing.YAML;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.lib.Config;
import group.aelysium.rustyconnector.plugin.paper.lib.PaperServer;
import group.aelysium.rustyconnector.plugin.paper.lib.database.Redis;
import ninja.leaping.configurate.ConfigurationNode;

public class GenericParser {
    public static void parse(Config config) throws IllegalArgumentException, IllegalAccessException {
        PaperRustyConnector plugin = PaperRustyConnector.getInstance();

        ConfigurationNode configData = config.getData();
        plugin.logger().log("---------| Preparing Server Data...");
        String address = YAML.get(configData,"server.address").getString();
        if(address.equals("")) throw new IllegalArgumentException("You must define an `address`. Copy the IP Address associated with this server and paste it into the `address` field in your `config.yml`");

        String privateKey = YAML.get(configData,"private-key").getString();

        String serverName = YAML.get(configData,"server.name").getString();
        if(serverName.equals("")) {
            plugin.logger().log("---------| There's no name defined for this server! Writing one now!");
            String trimmedHash = MD5.generatePrivateKey().substring(32);
            YAML.get(configData,"server.name").setValue(trimmedHash);

            serverName = trimmedHash;
        } else {
            plugin.logger().log("-----------| Registered server name as: "+serverName);
        }

        String family = YAML.get(configData,"family").getString();

        plugin.logger().log("---------| Finished!");

        plugin.logger().log("---------| Preparing Redis...");
        Redis redis = new Redis();

        redis.setConnection(
                YAML.get(configData,"redis.host").getString(),
                YAML.get(configData,"redis.port").getInt(),
                YAML.get(configData,"redis.password").getString(),
                YAML.get(configData,"redis.data-channel").getString()
        );
        redis.connect(plugin);

        PaperServer server = new PaperServer(
                serverName,
                privateKey,
                address,
                family,
                redis
        );

        int softPlayerCap = YAML.get(configData,"soft-player-cap").getInt();
        int hardPlayerCap = YAML.get(configData,"hard-player-cap").getInt();

        server.setPlayerCap(softPlayerCap, hardPlayerCap);

        plugin.setServer(server);

        plugin.logger().log("---------| Finished!");


        plugin.logger().log("---------| Checking for whitelist...");
        if(configData.getNode("use-whitelist").getBoolean()) {
            String whitelistName = configData.getNode("whitelist").getString();

            WhitelistParser.parse(whitelistName);

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a whitelist.");
        }
    }
}
