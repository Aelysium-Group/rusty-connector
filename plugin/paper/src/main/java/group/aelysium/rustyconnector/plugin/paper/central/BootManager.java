package group.aelysium.rustyconnector.plugin.paper.central;

import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.config.ConnectorsConfig;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.MessageCacheService;
import group.aelysium.rustyconnector.core.lib.serviceable.Service;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.Core;
import group.aelysium.rustyconnector.plugin.paper.lib.magic_link.MagicLinkService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.PacketBuilderService;
import group.aelysium.rustyconnector.plugin.paper.lib.services.ServerInfoService;
import group.aelysium.rustyconnector.plugin.paper.lib.dynamic_teleport.DynamicTeleportService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootManager {
    public static Core init() throws IllegalAccessException {
        PaperAPI api = PaperAPI.get();
        PluginLogger logger = api.logger();
        Map<Class<? extends Service>, Service> services = new HashMap<>();
        List<String> requestedConnectors = new ArrayList<>();

        // Setup private key
        char[] privateKey;
        {
            PrivateKeyConfig privateKeyConfig = PrivateKeyConfig.newConfig(new File(String.valueOf(api.dataFolder()), "private.key"));
            if (!privateKeyConfig.generate())
                throw new IllegalStateException("Unable to load or create private.key!");
            try {
                privateKey = privateKeyConfig.get();
            } catch (Exception ignore) {
                throw new IllegalAccessException("There was a fatal error while reading private.key!");
            }
        }

        // Setup default config
        DefaultConfig defaultConfig = DefaultConfig.newConfig(new File(String.valueOf(api.dataFolder()), "config.yml"), "paper_config_template.yml");
        {
            if (!defaultConfig.generate())
                throw new IllegalStateException("Unable to load or create config.yml!");
            defaultConfig.register();
        }

        // Setup connectors
        {
            logger.send(Component.text("Building Connectors...", NamedTextColor.DARK_GRAY));

            ConnectorsConfig connectorsConfig = ConnectorsConfig.newConfig(new File(String.valueOf(api.dataFolder()), "connectors.yml"), "velocity_connectors_template.yml");
            if (!connectorsConfig.generate())
                throw new IllegalStateException("Unable to load or create connectorsConfig.yml!");
            services.put(ConnectorsService.class, connectorsConfig.register(privateKey, logger, true, false));

            requestedConnectors.add(defaultConfig.getMessenger());

            logger.send(Component.text("Finished building Connectors.", NamedTextColor.GREEN));
        }

        {
            ServerInfoService serverInfoService = new ServerInfoService(
                    defaultConfig.getServer_name(),
                    AddressUtil.parseAddress(defaultConfig.getServer_address()),
                    defaultConfig.getServer_family(),
                    defaultConfig.getServer_playerCap_soft(),
                    defaultConfig.getServer_playerCap_hard(),
                    defaultConfig.getServer_weight()
            );
            services.put(ServerInfoService.class, serverInfoService);
        }

        {
            services.put(MessageCacheService.class, new MessageCacheService(50));
            logger.log("Set message cache size to be: 50");
        }

        {
            services.put(PacketBuilderService.class, new PacketBuilderService());
        }

        {
            services.put(DynamicTeleportService.class, new DynamicTeleportService());
        }

        {
            services.put(MagicLinkService.class, new MagicLinkService(3));
        }

        // Verify Connectors
        {
            logger.send(Component.text("Validating Connector service...", NamedTextColor.DARK_GRAY));
            ConnectorsService connectorsService = ((ConnectorsService) services.get(ConnectorsService.class));

            /*
             * Make sure that configs aren't trying to access connectors which don't exist.
             * Also makes sure that, if there are excess connectors defined, we only load and attempt to boot the ones that are actually being called.
             */
            for (String name : requestedConnectors) {
                logger.send(Component.text(" | Checking and building connector ["+name+"]...", NamedTextColor.DARK_GRAY));

                if(!connectorsService.containsKey(name))
                    throw new RuntimeException("No connector with the name '"+name+"' was found!");

                Connector connector = connectorsService.get(name);
                try {
                    connector.connect();
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.send(Component.text("Finished validating Connector service.", NamedTextColor.GREEN));
        }

        DefaultConfig.empty();

        return new Core(services, defaultConfig.getMessenger());
    }
}
