package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.core.lib.generic.firewall.MessageTunnel;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.lib.generic.parsing.YAML;

import java.net.InetSocketAddress;
import java.util.List;

public class GenericParser {
    public static void parse(Config config) throws IllegalAccessException {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        ConfigurationNode configData = config.getData();
        plugin.logger().log("-------| Configuring Proxy...");
        plugin.logger().log("---------| Preparing Families...");
        FamilyParser.parse(config);

        plugin.logger().log("---------| Registering root family of proxy...");
        String rootFamilyString = YAML.get(configData,"root-family").getString();
        ServerFamily rootFamily = ServerFamily.findFamily(rootFamilyString);
        if(rootFamily == null) throw new NullPointerException("Root family is referencing a family that doesn't exist! Make sure if you list a `root-family` you also include it's name in `families`.");

        plugin.getProxy().setRootFamily(rootFamily);
        plugin.logger().log("---------| Finished!");

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


        plugin.logger().log("---------| Preparing Proxy Whitelist...");
        if(configData.getNode("use-whitelist").getBoolean()) {
            String whitelistName = configData.getNode("whitelist").getString();
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
