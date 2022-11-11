package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.lib.parsing.YAML;
import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;

import java.io.File;
import java.util.List;

public class FamilyParser {
    public static void parse(Config config) {
        VelocityRustyConnector plugin = VelocityRustyConnector.getInstance();

        ConfigurationNode configData = config.getData();
        plugin.logger().log("---------| Preparing Families...");
        plugin.logger().log("-----------| Getting family names...");


        List<String> familyNames = (List<String>) configData.getNode("families").getValue();
        assert familyNames != null;

        plugin.logger().log("-----------| Loading families matching the names...");
        familyNames.forEach(name -> {
            plugin.logger().log("-------------| Loading: "+name+"...");
            try {
                Config familyConfig = new Config(new File(plugin.getDataFolder(), "families/"+name+".yml"), "velocity_family_template.yml");
                if(!familyConfig.register()) throw new RuntimeException("Unable to register "+name+".yml");

                AlgorithmType algorithm = AlgorithmType.valueOf((String) YAML.get(familyConfig.getData(),"load-balancing.algorithm").getValue());

                boolean shouldUseWhitelist = familyConfig.getData().getNode("use-whitelist").getBoolean();
                String whitelistName = familyConfig.getData().getNode("whitelist").getString();

                if(shouldUseWhitelist) {
                    WhitelistParser.parse(whitelistName);

                    Whitelist whitelist = plugin.getProxy().getWhitelistManager().find(whitelistName);

                    ServerFamily family = new ServerFamily(
                            name,
                            algorithm,
                            whitelist
                            );
                    plugin.getProxy().getFamilyManager().add(family);
                    plugin.logger().log("-----------| Finished!");
                    return;
                }

                ServerFamily family = new ServerFamily(
                        name,
                        algorithm,
                        null
                );
                plugin.getProxy().getFamilyManager().add(family);
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
