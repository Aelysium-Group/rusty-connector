package group.aelysium.rustyconnector.plugin.paper.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.rustyconnector.common.crypt.AES;
import group.aelysium.rustyconnector.common.crypt.NanoID;
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
import java.util.UUID;

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
            "# The family to register this server to.",
            "# Every server must be a member of a family.",
            "# If the server tries to register into a family that doesn't exist, the server won't register.",
            "#"
    })
    @Node(2)
    private String family = "lobby";

    @Comment({
            "#",
            "# The address for the Magic Link access point.",
            "# It's not necessary to provide a slash at the end.",
            "#",
            "# Example: http://127.0.0.1:8080",
            "#"
    })
    @Node(3)
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

    @Comment({
            "#",
            "# Should this server us a UUID for it's serverID instead of the family name + nano id?",
            "# This setting is really only necessary if you're in highly scalable environments and have",
            "# the infrastructure to properly handle UUIDs as server names.",
            "#",
            "# Enabling this setting is the same as setting this server's name in velocity.toml to a UUID.",
            "# Certain plugins aren't written to deal with server names that long and you'll have to implement",
            "# proper API support to display something other than server UUIDs to players.",
            "#",
            "# If you enable this, you'll need to delete `metadata/server.id` so that it can be regenerated with the new uuid.",
            "#"
    })
    @Node(4)
    private boolean useUUID = false;

    @Node(5)
    @Comment({
            "#",
            "# Provide additional metadata for the server",
            "# Metadata provided here is non-essential, meaning that RustyConnector is capable of running without anything provided here.",
            "# When you provide metadata here it will be sent to the proxy. Ensure that the provided metadata conforms to valid JSON syntax.",
            "#",
            "# For built-in metadata options, check the Aelysium wiki:",
            "# https://wiki.aelysium.group/rusty-connector/docs/concepts/metadata/",
            "#"
    })
    private String metadata = "{\\\"softCap\\\": 30, \\\"hardCap\\\": 40}";

    public ServerKernel.Tinder data(Server server, PluginLogger logger) throws Exception {
        if(this.family.isBlank()) throw new IllegalArgumentException("Please provide a valid family name to target.");
        if(this.family.length() > 16) throw new IllegalArgumentException("Family names are not allowed to be larger than 16 characters.");

        ServerIDConfig idConfig = ServerIDConfig.Read();
        String id = (idConfig == null ? null : idConfig.id());
        if(id == null) {
            if (this.useUUID) {
                id = UUID.randomUUID().toString();
            } else {
                int extra = 16 - this.family.length();
                NanoID nanoID = NanoID.randomNanoID(15 + extra); // 15 because there's a `-` separator between family name and nanoid
                id = this.family + "-" + nanoID;
            }

            ServerIDConfig.Load(id);
        }

        AES aes = PrivateKeyConfig.New().cryptor();
        WebSocketMagicLink.Tinder magicLink = new WebSocketMagicLink.Tinder(
                URL.parseURL(this.magicLink_accessEndpoint),
                Packet.SourceIdentifier.server(id),
                aes,
                new PacketCache(100),
                null
                //this.magicLink_broadcastingAddress.isEmpty() ? null : new IPV6Broadcaster(cryptor, AddressUtil.parseAddress(this.magicLink_broadcastingAddress))
        );

        ServerKernel.Tinder tinder = new ServerKernel.Tinder(
                id,
                new PaperServerAdapter(server, logger),
                AddressUtil.parseAddress(this.address),
                magicLink,
                this.family
        );

        Gson gson = new Gson();
        JsonObject metadataJson = gson.fromJson(this.metadata, JsonObject.class);
        metadataJson.entrySet().forEach(e->tinder.metadata(e.getKey(), Packet.Parameter.fromJSON(e.getValue())));

        return tinder;
    }

    public static DefaultConfig New() throws IOException {
        return DeclarativeYAML.load(DefaultConfig.class);
    }
}
