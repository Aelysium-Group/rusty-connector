package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.ServerFamily;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.generic.lib.generic.parsing.YAML;
import group.aelysium.rustyconnector.core.generic.lib.generic.whitelist.Whitelist;
import group.aelysium.rustyconnector.core.generic.lib.generic.load_balancing.AlgorithmType;

import java.io.File;
import java.util.List;

public class FamilyParser {
    public static void parse(Config config, VelocityRustyConnector plugin) {
        ConfigurationNode configData = config.getData();
        plugin.logger().log("---------| Preparing Families...");
        plugin.logger().log("-----------| Getting family names...");


        List<String> familyNames = (List<String>) configData.getNode("families").getValue();
        assert familyNames != null;

        plugin.logger().log("-----------| Loading families matching the names...");
        familyNames.forEach(name -> {
            plugin.logger().log("-------------| Loading: "+name+"...");
            try {
                Config familyConfig = new Config(plugin, new File(plugin.getDataFolder(), "families/"+name+".yml"), "template_family.yml");
                if(!familyConfig.register()) throw new RuntimeException("Unable to register "+name+".yml");

                AlgorithmType algorithm = AlgorithmType.valueOf((String) YAML.get(familyConfig.getData(),"load-balancing.algorithm").getValue());

                boolean shouldUseWhitelist = (boolean) familyConfig.getData().getNode("use-whitelist").getValue();
                String whitelistName = (String) familyConfig.getData().getNode("whitelist").getValue();

                if(shouldUseWhitelist) {
                    plugin.logger().log("---------------| Getting whitelist: "+whitelistName+"...");
                    Config whitelistConfig = new Config(plugin, new File(plugin.getDataFolder(), "whitelists/"+whitelistName+".yml"), "template_whitelist.yml");
                    if(!whitelistConfig.register()) throw new RuntimeException("Unable to register the defined whitelist");

                    if(!WhitelistParser.parse(whitelistName, whitelistConfig, plugin)) throw new NullPointerException("The requested whitelist is invalid or doesn't exist! Is it configured properly?");

                    Whitelist whitelist = plugin.getProxy().getWhitelist(whitelistName);

                    ServerFamily family = new ServerFamily(
                            name,
                            algorithm,
                            whitelist
                            );
                    plugin.getProxy().registerFamily(family);
                    plugin.logger().log("-----------| Finished!");
                    return;
                }

                ServerFamily family = new ServerFamily(
                        name,
                        algorithm,
                        null
                );
                plugin.getProxy().registerFamily(family);
                plugin.logger().log("-----------| Finished! Family doesn't have a whitelist.");
                return;
            } catch (NullPointerException e) {
                plugin.logger().log("Unable to register the family: "+name);
                plugin.logger().error("One of the data types provided in this family's config is invalid and not what was expected!",e);
            } catch (Exception e) {
                plugin.logger().error("Unable to register the family: "+name,e);
            }
        });
        plugin.logger().log("---------| Finished loading families!");
    }
}
