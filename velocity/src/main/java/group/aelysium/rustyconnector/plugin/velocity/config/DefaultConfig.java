package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.annotations.*;

import java.io.IOException;

@Namespace("rustyconnector")
@Config("/config.yml")
public class DefaultConfig {
    @Comment({
            "###########################################################################################################",
            "#|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "###########################################################################################################",
            "#                                                                                                         #",
            "#   /███████                           /██                                                                #",
            "#  | ██__  ██                         | ██                                                                #",
            "#  | ██  \\ ██  /██   /██   /███████  /██████    /██   /██                                                 #",
            "#  | ███████/ | ██  | ██  /██_____/ |_  ██_/   | ██  | ██                                                 #",
            "#  | ██__  ██ | ██  | ██ |  ██████    | ██     | ██  | ██                                                 #",
            "#  | ██  \\ ██ | ██  | ██  \\____  ██   | ██ /██ | ██  | ██                                                 #",
            "#  | ██  | ██ |  ██████/  /███████/   |  ████/ |  ███████                                                 #",
            "#  |__/  |__/  \\______/  |_______/     \\___/    \\____  ██                                                 #",
            "#                                               /██  | ██                                                 #",
            "#                                              |  ██████/                                                 #",
            "#    /██████                                    \\______/                /██                               #",
            "#   /██__  ██                                                          | ██                               #",
            "#  | ██  \\__/   /██████   /███████   /███████    /██████    /███████  /██████     /██████    /██████      #",
            "#  | ██        /██__  ██ | ██__  ██ | ██__  ██  /██__  ██  /██_____/ |_  ██_/    /██__  ██  /██__  ██     #",
            "#  | ██       | ██  \\ ██ | ██  \\ ██ | ██  \\ ██ | ████████ | ██         | ██     | ██  \\ ██ | ██  \\__/     #",
            "#  | ██    ██ | ██  | ██ | ██  | ██ | ██  | ██ | ██_____/ | ██         | ██ /██ | ██  | ██ | ██           #",
            "#  |  ██████/ |  ██████/ | ██  | ██ | ██  | ██ |  ███████ |  ███████   |  ████/ |  ██████/ | ██           #",
            "#   \\______/   \\______/  |__/  |__/ |__/  |__/  \\_______/  \\_______/    \\___/    \\______/  |__/           #",
            "#                                                                                                         #",
            "#                                                                                                         #",
            "#                                        Welcome to RustyConnector!                                       #",
            "#                            https://github.com/Aelysium-Group/rusty-connector                            #",
            "#                                                                                                         #",
            "#                            -------------------------------------------------                            #",
            "#                                                                                                         #",
            "# | RustyConnector is your go-to load-balancer and     | Built for usage on high-traffic networks;        #",
            "# | player manager for Minecraft networks!             | RustyConnector is meant to be scalable and easy  #",
            "#                                                      | to maintain!                                     #",
            "#                                                                                                         #",
            "#                              | If you ever need assistance setting up your                              #",
            "#                              | plugin come join us in our discord server:                               #",
            "#                              | https://join.aelysium.group/                                             #",
            "#                                                                                                         #",
            "#                            -------------------------------------------------                            #",
            "#                                                                                                         #",
            "#                                          Produced by: Aelysium                                          #",
            "#                                                                                                         #",
            "#                            -------------------------------------------------                            #",
            "#                                                                                                         #",
            "###########################################################################################################",
            "#|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "###########################################################################################################",
            "#",
            "# If you need help updating your configs from an older version;",
            "# take a look at our config migration docs:",
            "#",
            "# https://wiki.aelysium.group/rusty-connector/docs/updating/",
            "#"
    })
    @Node()
    public int version = 7;

    @Comment({
        "#",
        "# The name of the root family which all players initially connecting to the network will join into.",
        "#"
    })
    @Node(1)
    public String rootFamily = "lobby";
    
    @Comment({
        "#",
        "# The directory that RustyConnector should scan for native modules.",
        "# Native modules are similar to Minecraft plugins except they've been written specifically for RustyConnector and do not depend on Minecraft code at all.",
        "#"
    })
    @Node(2)
    public String moduleDirectory = "/rc-module";
    
    @Comment({
        "#",
        "# The directory that RustyConnector should provide for modules to store their configs in.",
        "# Modules are able to ignore this setting if they really want, but well-written modules",
        "# will store their configs in the directory you provide inside of their own dedicated directory",
        "# Native modules are similar to Minecraft plugins except they've been written specifically for RustyConnector and do not depend on Minecraft code at all.",
        "#"
    })
    @Node(3)
    public String moduleConfigDirectory = "/rc-module";

    public static DefaultConfig New() throws IOException {
        return DeclarativeYAML.From(DefaultConfig.class);
    }
}