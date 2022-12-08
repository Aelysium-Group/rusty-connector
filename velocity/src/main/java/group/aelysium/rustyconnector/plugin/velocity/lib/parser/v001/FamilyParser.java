package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.LeastConnection;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.RoundRobin;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.lib.parsing.YAML;
import group.aelysium.rustyconnector.core.lib.firewall.Whitelist;
import group.aelysium.rustyconnector.core.lib.load_balancing.AlgorithmType;

import java.io.File;
import java.util.List;
import java.util.Objects;

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

                boolean shouldUseWhitelist = familyConfig.getData().getNode("use-whitelist").getBoolean();
                String whitelistName = familyConfig.getData().getNode("whitelist").getString();

                String algorithmName = String.valueOf(YAML.get(familyConfig.getData(),"load-balancing.algorithm").getValue());

                Whitelist whitelist = null;
                if(shouldUseWhitelist) {
                    WhitelistParser.parse(whitelistName);

                    whitelist = plugin.getProxy().getWhitelistManager().find(whitelistName);

                    plugin.logger().log("-----------| Family has a whitelist.");
                } else {
                    plugin.logger().log("-----------| Family doesn't have a whitelist.");
                }

                switch (Enum.valueOf(AlgorithmType.class, algorithmName)) {
                    case ROUND_ROBIN -> plugin.getProxy().getFamilyManager().add(
                            new ServerFamily<>(
                                    name,
                                    whitelist,
                                    RoundRobin.class
                            )
                    );
                    case LEAST_CONNECTION -> plugin.getProxy().getFamilyManager().add(
                            new ServerFamily<>(
                                    name,
                                    whitelist,
                                    LeastConnection.class
                            )
                    );
                }
                plugin.logger().log("-----------| Finished!");
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
