package group.aelysium.rustyconnector.plugin.paper.config;

import group.aelysium.rustyconnector.common.IPV6Broadcaster;
import group.aelysium.rustyconnector.common.absolute_redundancy.Particle;
import group.aelysium.rustyconnector.common.cache.MessageCache;
import group.aelysium.rustyconnector.common.config.Comment;
import group.aelysium.rustyconnector.common.config.Config;
import group.aelysium.rustyconnector.common.config.ConfigLoader;
import group.aelysium.rustyconnector.common.config.Node;
import group.aelysium.rustyconnector.common.crypt.AES;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.paper.PaperServerAdapter;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;
import group.aelysium.rustyconnector.server.ServerFlame;
import group.aelysium.rustyconnector.server.magic_link.WebSocketMagicLink;
import org.bukkit.Server;

import java.io.IOException;

@Config("config.yml")
public class DefaultConfig {
    @Comment({
            "#" +
            "# If you need help updating your configs from an older version;" +
            "# take a look at our config migration docs:" +
            "#" +
            "# https://wiki.aelysium.group/rusty-connector/docs/updating/" +
            "#"
    })
    @Node(key = "version", defaultValue = "7")
    private int version;

    @Comment({
            "#",
            "# The address used to connect to this server.",
            "# This address should match what a player would enter if they were trying to connect directly to this server.",
            "# Make sure you also include the port number!",
            "#",
            "# If you're in a Kubernetes or Docker environment, you can bypass this option by setting the",
            "#",
            "# Example: 127.0.0.1:25565",
            "#"
    })
    @Node(order = 1, key = "address", defaultValue = "default")
    private String address;

    @Comment({
            "#",
            "# The address used to connect to this server.",
            "# This address should match what a player would enter if they were trying to connect directly to this server.",
            "# Make sure you also include the port number!",
            "#",
            "# If you're in a Kubernetes or Docker environment, you can bypass this option by setting the",
            "#",
            "# Example: 127.0.0.1:25565",
            "#"
    })
    @Node(order = 2, key = "display-name", defaultValue = "")
    private String displayName;

    @Comment({
            "#",
            "# Define the MCLoader Config to load from the proxy.",
            "# To define new custom configs in the proxy look in the \"magic_configs\" folder.",
            "# The definition below can contain \".yml\" or not, it doesn't matter.",
            "#"
    })
    @Node(order = 3, key = "server-registration", defaultValue = "default")
    private String serverRegistration;

    @Comment({
            "#",
            "# The address for the Magic Link access point.",
            "# It's not necessary to provide a slash at the end.",
            "#",
            "# Example: http://127.0.0.1:8080",
            "#"
    })
    @Node(order = 4, key = "magic-link.access-endpoint", defaultValue = "http://127.0.0.1:8080")
    private String magicLinkEndpoint;

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
    @Node(order = 5, key = "magic-link.broadcasting-address", defaultValue = "")
    private String broadcastingAddress;

    public ServerFlame.Tinder data(Server server, PluginLogger logger) throws IOException {
        AES cryptor = PrivateKeyConfig.New().cryptor();
        WebSocketMagicLink.Tinder magicLink = new WebSocketMagicLink.Tinder(
                this.magicLinkEndpoint,
                Packet.Target.server(ServerUUIDConfig.New().uuid()),
                cryptor,
                new MessageCache(100),
                this.serverRegistration,
                new IPV6Broadcaster(cryptor, AddressUtil.parseAddress(this.broadcastingAddress))
        );

        return new ServerFlame.Tinder(
                ServerUUIDConfig.New().uuid(),
                new PaperServerAdapter(server, logger),
                this.displayName,
                AddressUtil.parseAddress(this.address),
                magicLink
        );
    }

    public static DefaultConfig New() throws IOException {
        return ConfigLoader.load(DefaultConfig.class);
    }
}
