package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.rustyconnector.common.crypt.AES;
import group.aelysium.rustyconnector.common.magic_link.PacketCache;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.common.util.URL;
import group.aelysium.rustyconnector.plugin.common.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.common.config.ServerIDConfig;
import group.aelysium.rustyconnector.plugin.paper.PaperServerAdapter;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;
import group.aelysium.rustyconnector.server.ServerKernel;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import org.bukkit.Server;

import java.io.IOException;
import java.text.ParseException;

@Config("plugins/rustyconnector/config.yml")
@Git(value = "rustyconnector", required = false)
public class DefaultConfig {
    @Comment({
            "#",
            "# If you need help updating your configs from an older version;",
            "# take a look at our config migration docs:",
            "#",
            "# https://wiki.aelysium.group/rusty-connector/docs/updating/",
            "#"
    })
    @Node()
    private int version = 7;

    @Comment({
            "#",
            "# The address used to connect to this server.",
            "# This address should match what a player would enter if they were trying to connect directly to this server.",
            "# Make sure you also include the port number!",
            "#",
            "# If you're in a Kubernetes or Docker environment, you can bypass this option by setting the RC_ADDRESS environment variable.",
            "#",
            "# Example: 127.0.0.1:25565",
            "#"
    })
    @Node(1)
    private String address = "127.0.0.1:25565";

    @Comment({
            "#",
            "# An optional display name that can be used to represent this server to users.",
            "# If you do not provide a display name, the server's ID will be used instead.",
            "# Display names are allowed to say whatever you want, they are also allowed to be duplicate across multiple servers.",
            "# Display names only exist as a visual sugar. They carry no significance outside of looks.",
            "#"
    })
    @Node(2)
    private String displayName = "";

    @Comment({
            "#",
            "# Define the Registration Config to load from the proxy.",
            "# The name you define here corresponds with a config on the proxy.",
            "# When this server registers, the settings in that config will be applied.",
            "# To define new custom configs in the proxy look in the \"magic_configs\" folder.",
            "#"
    })
    @Node(3)
    private String serverRegistration = "default";

    @Comment({
            "#",
            "# The address for the Magic Link access point.",
            "# It's not necessary to provide a slash at the end.",
            "#",
            "# Example: http://127.0.0.1:8080",
            "#"
    })
    @Node(4)
    private String magicLink_accessEndpoint = "http://127.0.0.1:8080";
/*
    @Comment({
            "#",
            "# This setting should only be used if endpoint broadcasting has been enabled on the Proxy.",
            "#",
            "# The address to use for Magic Link endpoint broadcasting.",
            "# This address should match what's defined on the proxy in the Magic Link config.",
            "# Make sure you also include the port number and ensure you follow the URI specifications for IPv6 addresses.",
            "#",
            "# Leave this empty to disable it.",
            "#",
            "# Example: [FF02::1]:4446",
            "#"
    })
    @Node(5)*/
    private String magicLink_broadcastingAddress = "";

    public ServerKernel.Tinder data(Server server, PluginLogger logger) throws IOException, ParseException {
        AES cryptor = PrivateKeyConfig.New().cryptor();
        WebSocketMagicLink.Tinder magicLink = new WebSocketMagicLink.Tinder(
                URL.parseURL(this.magicLink_accessEndpoint),
                Packet.SourceIdentifier.server(ServerIDConfig.New().id()),
                cryptor,
                new PacketCache(100),
                this.serverRegistration,
                null
                //this.magicLink_broadcastingAddress.isEmpty() ? null : new IPV6Broadcaster(cryptor, AddressUtil.parseAddress(this.magicLink_broadcastingAddress))
        );

        return new ServerKernel.Tinder(
                ServerIDConfig.New().id(),
                new PaperServerAdapter(server, logger),
                this.displayName,
                AddressUtil.parseAddress(this.address),
                magicLink
        );
    }

    public static DefaultConfig New() throws IOException {
        return DeclarativeYAML.load(DefaultConfig.class);
    }
}
