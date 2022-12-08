package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.core.lib.firewall.MessageTunnel;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.ConfigFileLoader;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.Redis;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.lib.parsing.YAML;

import java.net.InetSocketAddress;
import java.util.List;

public class GenericParser {
    public static void parse(ConfigFileLoader configFileLoader) throws IllegalAccessException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        ConfigurationNode configData = configFileLoader.getData();
        plugin.logger().log("-------| Configuring Proxy...");
        plugin.logger().log("---------| Preparing Families...");
        FamilyParser.parse(configFileLoader);

        plugin.logger().log("---------| Registering root family of proxy...");
        String rootFamilyString = YAML.get(configData,"root-family").getString();
        plugin.getProxy().setRootFamily(rootFamilyString);
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

        plugin.getProxy().setRedis(redis);

        plugin.getProxy().startHeart(YAML.get(configData,"heart-beat").getLong());

        plugin.logger().log("---------| Finished!");


        plugin.logger().log("---------| Preparing Proxy Whitelist...");
        if(configData.getNode("use-whitelist").getBoolean()) {
            String whitelistName = configData.getNode("whitelist").getString();
            plugin.getProxy().setWhitelist(whitelistName);

            WhitelistParser.parse(whitelistName);

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a whitelist.");
        }


        plugin.logger().log("---------| Preparing Proxy Messaging Tunnel...");
        if(YAML.get(configData,"message-tunnel.enabled").getBoolean()) {

            List<String> whitelist = (List<String>) YAML.get(configData,"message-tunnel.whitelist").getValue();
            List<String> blacklist = (List<String>) YAML.get(configData,"message-tunnel.denylist").getValue();

            boolean useWhitelist = whitelist.size() > 0;
            boolean useBlacklist = blacklist.size() > 0;

            MessageTunnel messageTunnel = new MessageTunnel(useBlacklist, useWhitelist);

            plugin.setMessageTunnel(messageTunnel);

            whitelist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.whitelistAddress(address);
            });

            blacklist.forEach(entry -> {
                String[] addressSplit = entry.split(":");

                InetSocketAddress address = new InetSocketAddress(addressSplit[0], Integer.parseInt(addressSplit[1]));

                messageTunnel.blacklistAddress(address);
            });

            plugin.logger().log("---------| Finished!");
        } else {
            plugin.logger().log("---------| Finished! Proxy doesn't have a message tunnel.");
        }
    }


}
