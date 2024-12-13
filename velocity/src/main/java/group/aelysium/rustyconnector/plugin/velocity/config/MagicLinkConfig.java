package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.rustyconnector.common.crypt.AES;
import group.aelysium.rustyconnector.common.magic_link.PacketCache;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.plugin.common.config.PrivateKeyConfig;
import group.aelysium.rustyconnector.plugin.common.config.ServerIDConfig;
import group.aelysium.rustyconnector.proxy.magic_link.WebSocketMagicLink;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;

import java.io.IOException;
import java.util.*;

@Config("plugins/rustyconnector/magic_link.yml")
@Git(value = "rustyconnector", required = false)
@Comment({
    "#`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´#",
    "#.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·.#",
    "#>>>█>>>>>>>>>████>>>>>>>>>>>██>>>>>>>>>>>>>>█>>>>>>>>>>>>>>>███>>>>>>>>>>>>>█>>>>>>>>>>>██>>>>>>>>>█>>>#",
    "#`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´#",
    "#.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·.#",
    "#         __    __    ______    ______    __    ______      __        __    __   __    __  __           #",
    "#        /\\ \"-./  \\  /\\  __ \\  /\\  ___\\  /\\ \\  /\\  ___\\    /\\ \\      /\\ \\  /\\ \"-.\\ \\  /\\ \\/ /           #",
    "#        \\ \\ \\-./\\ \\ \\ \\  __ \\ \\ \\ \\__ \\ \\ \\ \\ \\ \\ \\____   \\ \\ \\____ \\ \\ \\ \\ \\ \\-.  \\ \\ \\  _\"-.         #",
    "#         \\ \\_\\ \\ \\_\\ \\ \\_\\ \\_\\ \\ \\_____\\ \\ \\_\\ \\ \\_____\\   \\ \\_____\\ \\ \\_\\ \\ \\_\\\\\"\\_\\ \\ \\_\\ \\_\\        #",
    "#          \\/_/  \\/_/  \\/_/\\/_/  \\/_____/  \\/_/  \\/_____/    \\/_____/  \\/_/  \\/_/ \\/_/  \\/_/\\/_/        #",
    "#                                                                                                       #",
    "#`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´#",
    "#.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·.#",
    "#<<<█<<<<<<<<<<██<<<<<<█<<<<<<<<██<<<<██<<<<<<███<<<<<<<<<<<<███<<<<<<<<<<<<<<<█████<<<<<<<<<<<<<<<<█<<<#",
    "#`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´#",
    "#.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·.#",
})
public class MagicLinkConfig {
    @Comment({
        "#",
        "# The address which servers will make requests to.",
        "# You should include the port number as well.",
        "#"
    })
    @Node
    private String address = "127.0.0.1:8080";


    @Comment({
        "#",
        "# Should Magic Link utilize SSL certification for secure connections.",
        "# All packets shipped over Magic Link are AES 256-bit encrypted no matter what.",
        "# However, it's just about always best practice to use secured connections",
        "# especially if the connection isn't over a private network.",
        "# `true` - Magic Link will utilize wss and https.",
        "# `false` - Magic Link will utilize ws and http.",
        "#"
    })
    @Node(1)
    private boolean secured = false;

/*
    @Comment({
            "#",
            "# For network configurations that support it, you can use IPv6 UDP Multicasting",
            "# to broadcast Magic Link's connection endpoint to other servers.",
            "# This saves you the hassle of manually defining the proxy endpoint in all of your Servers.",
            "# Once servers receive the connection details, they'll still be required to authenticate.",
            "# The connection details are AES-256 bit encrypted.",
            "#",
            "# If enabled, you'll also have to enable this setting on your Servers.",
            "#"
    })
    @Node(2)
    private boolean endpointBroadcasting_enabled = false;

    @Comment({
        "#",
        "# The address to use for the broadcasting.",
        "# This address must also be set on the Server.",
        "# Make sure you also include the port number! And ensure you follow the URI specifications for IPv6 addresses.",
        "#",
        "# Example: [FF02::1]:4446",
        "#"
    })
    @Node(3)
    private String endpointBroadcasting_address = "[FF02::1]:4446";

    @Comment({
        "#",
        "# The interval of time between which Magic Link connection details will be broadcasted.",
        "#"
    })
    @Node(4)
    private String endpointBroadcasting_interval = "15 SECONDS";
*/
    @Comment({
        "#",
        "# The number of packets that will be saved into memory at any time.",
        "# As new packets are received, old packets will get pushed out of the cache.",
        "#"
    })
    @Node(5)
    private int cache_size = 100;

    @Comment({
        "#",
        "# The packet types that shouldn't be cached.",
        "# If a packet is of a type that is contained below, it will not be cached.",
        "#"
    })
    @Node(6)
    private List<String> cache_ignoredTypes = new ArrayList<>();

    public WebSocketMagicLink.Tinder tinder() throws IOException {
        AES cryptor = PrivateKeyConfig.New().cryptor();


        List<Packet.Type> ignoredTypes = new ArrayList<>();
        this.cache_ignoredTypes.forEach(s -> {
            String[] split = s.split("-");
            ignoredTypes.add(Packet.Type.from(split[0], split[1]));
        });

        return new WebSocketMagicLink.Tinder(
                AddressUtil.parseAddress(this.address),
                Packet.SourceIdentifier.proxy(ServerIDConfig.Load(UUID.randomUUID().toString()).id()),
                cryptor,
                new PacketCache(this.cache_size, ignoredTypes),
                null
                //this.endpointBroadcasting_enabled ? new IPV6Broadcaster(cryptor, AddressUtil.parseAddress(this.endpointBroadcasting_address)) : null
        );
    }

    public static MagicLinkConfig New() throws IOException {
        return DeclarativeYAML.load(MagicLinkConfig.class);
    }
}
