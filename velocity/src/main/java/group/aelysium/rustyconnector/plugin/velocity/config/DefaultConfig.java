package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;

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
    private int version = 7;

    @Comment({
            "#" +
            "# The name of the root family which all players initially connecting to the network will join into." +
            "#"
    })
    @Node(1)
    private String rootFamily = "lobby";

    public int version() {
        return version;
    }

    public String rootFamily() {
        return this.rootFamily;
    }

    public static DefaultConfig New() throws IOException {
        return DeclarativeYAML.From(DefaultConfig.class);
    }
}