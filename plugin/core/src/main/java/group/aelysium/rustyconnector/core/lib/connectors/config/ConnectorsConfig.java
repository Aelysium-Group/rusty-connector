package group.aelysium.rustyconnector.core.lib.connectors.config;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.config.YAML;
import group.aelysium.rustyconnector.core.lib.connectors.Connector;
import group.aelysium.rustyconnector.core.lib.connectors.ConnectorsService;
import group.aelysium.rustyconnector.core.lib.connectors.UserPass;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.redis.RedisConnector;
import group.aelysium.rustyconnector.core.lib.connectors.implementors.messenger.websocket.WebSocketConnector;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import io.lettuce.core.protocol.ProtocolVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.net.InetSocketAddress;

public class ConnectorsConfig extends YAML {
    private static ConnectorsConfig config;

    private ConnectorsConfig(File configPointer, String template) {
        super(configPointer, template);
    }

    public static ConnectorsConfig getConfig() {
        return config;
    }

    public static ConnectorsConfig newConfig(File configPointer, String template) {
        config = new ConnectorsConfig(configPointer, template);
        return ConnectorsConfig.getConfig();
    }

    @SuppressWarnings("unchecked")
    public ConnectorsService register(char[] privateKey, PluginLogger logger, boolean loadMessengers, boolean loadStorage) throws IllegalStateException {
        ConnectorsService connectorsService = new ConnectorsService();

        if(loadMessengers)
            YAML.get(this.data,"messengers").getChildrenList().forEach(node -> {
                String name = this.getNode(node, "name", String.class);
                try {
                    ConnectorsService.MessengerConnectors type = ConnectorsService.MessengerConnectors.valueOf(this.getNode(node, "type", String.class).toUpperCase());
                    String host = this.getNode(this.data, "host", String.class);
                    if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
                    int port = this.getNode(this.data, "port", Integer.class);
                    InetSocketAddress address = new InetSocketAddress(host, port);

                    String user = this.getNode(this.data, "user", String.class);
                    if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
                    char[] password = this.getNode(this.data, "password", String.class).toCharArray();
                    UserPass userPass = new UserPass(user, password);

                    switch (type) {
                        case REDIS -> {
                            ProtocolVersion protocol = ProtocolVersion.RESP2;
                            try {
                                protocol = ProtocolVersion.valueOf(this.getNode(this.data, "protocol", String.class));
                            } catch (Exception ignore) {}

                            String dataChannel = this.getNode(this.data, "data-channel", String.class);
                            if (dataChannel.equals("")) throw new IllegalStateException("Please configure your connector settings. `dataChannel` cannot be empty for Redis connectors.");

                            connectorsService.add(name, (Connector) RedisConnector.create(address, userPass, protocol, dataChannel, privateKey));
                        }
                        case RABBITMQ -> {
                        }
                        case WEBSOCKET -> {
                            char[] connectKey = null;
                            try {
                                connectKey = this.getNode(this.data, "key", String.class).toCharArray();
                            } catch (Exception ignore) {}

                            connectorsService.add(name, (Connector) WebSocketConnector.create(address, connectKey, privateKey));
                        }
                    }
                } catch (Exception e) {
                    logger.send(Lang.BOXED_MESSAGE_COLORED.build("There was an issue while building the messenger "+name+": "+e.getMessage(), NamedTextColor.RED));
                }
            });

        if(loadStorage)
            get(this.data,"storage").getChildrenList().forEach(node -> {
                String name = this.getNode(node, "name", String.class);
                try {
                    ConnectorsService.StorageConnectors type = ConnectorsService.StorageConnectors.valueOf(this.getNode(node, "type", String.class).toUpperCase());
                    String host = this.getNode(this.data, "host", String.class);
                    if (host.equals("")) throw new IllegalStateException("Please configure your connector settings. `host` cannot be empty.");
                    int port = this.getNode(this.data, "port", Integer.class);
                    InetSocketAddress address = new InetSocketAddress(host, port);

                    String user = this.getNode(this.data, "user", String.class);
                    if (user.equals("")) throw new IllegalStateException("Please configure your connector settings. `user` cannot be empty.");
                    char[] password = this.getNode(this.data, "password", String.class).toCharArray();
                    UserPass userPass = new UserPass(user, password);

                    switch (type) {
                        case MYSQL -> {

                        }
                    }
                } catch (Exception e) {
                    logger.send(Lang.BOXED_MESSAGE_COLORED.build("There was an issue while building the storage "+name+": "+e.getMessage(), NamedTextColor.RED));
                }
            });

        return connectorsService;
    }
}
