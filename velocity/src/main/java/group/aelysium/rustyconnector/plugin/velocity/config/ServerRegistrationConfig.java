package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.declarative_yaml.lib.Printer;
import group.aelysium.rustyconnector.common.magic_link.MagicLinkCore;

import java.util.Map;

@Config("plugins/rustyconnector/server_registrations/{name}.yml")
@Git(value = "rustyconnector", required = false)
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                   Server Registration                    #",
        "#                                                          #",
        "#               ---------------------------                #",
        "# | Server Registrations contain proxy-side details that   #",
        "# | servers inherit when they register through Magic Link. #",
        "#                                                          #",
        "# | All servers are required to have a Server Registration #",
        "# | which defines details such as soft/hard player caps    #",
        "# | and the server's family.                               #",
        "#                                                          #",
        "# | Changing details in a Server Registration Config       #",
        "# | will update those settings for all servers using it.   #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################"
})
public class ServerRegistrationConfig {
    @PathParameter("name")
    private String name;

    @Comment({
            "#",
            "# The family that the server will be registered into.",
            "#"
    })
    @Node()
    private String family = "lobby";

    @Comment({
            "#",
            "# The server's weight as it applies to load balancing.",
            "#"
    })
    @Node(1)
    private int weight = 0;

    @Comment({
            "#",
            "# At what point, should this server stop accepting regular players.",
            "# This allows you to mark the server as \"full\" while still allowing extra space for VIP and staff.",
            "#",
            "# To allow players to join past this point, you can give them one of two permissions:",
            "# rustyconnector.softCapBypass - Bypass the soft cap of any server on this network",
            "# rustyconnector.<family name>.softCapBypass - Bypass the soft cap of any server inside of this family",
            "#"
    })
    @Node(2)
    private int softCap = 20;

    @Comment({
            "#",
            "# At what point, should this server stop accepting soft-cap bypassing players.",
            "# if you wish to still allow high-level staff to access a server at hard player cap. You can grant them the permission:",
            "# rustyconnector.hardCapBypass - Bypass the hard cap of any server on this network",
            "# rustyconnector.<family name>.hardCapBypass - Bypass the hard cap of any server inside of this family. This also bypasses the soft cap too.",
            "#"
    })
    @Node(3)
    private int hardCap = 30;

    public MagicLinkCore.Proxy.ServerRegistrationConfiguration configuration() {
        return new MagicLinkCore.Proxy.ServerRegistrationConfiguration(
                this.family,
                this.weight,
                this.softCap,
                this.hardCap
        );
    }

    public static ServerRegistrationConfig New(String name) {
        Printer printer = new Printer()
                .pathReplacements(Map.of("name", name));
        return DeclarativeYAML.load(ServerRegistrationConfig.class, printer);
    }
}
