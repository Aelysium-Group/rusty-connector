package group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001;

import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import ninja.leaping.configurate.ConfigurationNode;
import group.aelysium.rustyconnector.core.generic.lib.generic.whitelist.Whitelist;

public class WhitelistParser {
    public static boolean parse(String configName, Config config, VelocityRustyConnector plugin) {
        try {
            ConfigurationNode configData = config.getData();
            boolean usePlayers = (boolean) configData.getNode("use-players").getValue();
            boolean usePermission = (boolean) configData.getNode("use-permission").getValue();
            boolean useCountry = (boolean) configData.getNode("use-country").getValue();

            Whitelist whitelist = new Whitelist(configName, usePlayers, usePermission, useCountry);

            if(usePlayers) WhitelistParser.parsePlayers(configData, plugin, whitelist);
            if(useCountry) WhitelistParser.parseCountries(configData, plugin, whitelist);

            plugin.getProxy().registerWhitelist(configName, whitelist);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void parsePlayers(ConfigurationNode configData, VelocityRustyConnector plugin, Whitelist whitelist) {
        //configData.getNode("players").getValue();
    }

    public static void parseCountries(ConfigurationNode configData, VelocityRustyConnector plugin, Whitelist whitelist) {

    }
}
