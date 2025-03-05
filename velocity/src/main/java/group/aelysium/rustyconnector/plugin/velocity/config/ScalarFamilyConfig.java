package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.rustyconnector.shaded.group.aelysium.declarative_yaml.lib.Printer;

import java.io.IOException;
import java.util.Map;

@Namespace("rustyconnector")
@Config("/scalar_families/{id}.yml")
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                      Scalar Family                       #",
        "#                                                          #",
        "#               ---------------------------                #",
        "# | Scalar families are optimized for stateless            #",
        "# | minecraft gamemodes.                                   #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################",
})
public class ScalarFamilyConfig {
    @PathParameter("id")
    public String id;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                       Display Name                       #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | Display name is the name of your family, as players    #",
            "# | will see it, in-game.                                  #",
            "# | Display name can appear as a result of multiple        #",
            "# | factors such as the friends module being enabled.      #",
            "#                                                          #",
            "# | Multiple families are allowed to have the              #",
            "# | same display name.                                     #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(0)
    public String displayName = "";

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                      Parent Family                       #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | The parent family is the family that players will      #",
            "# | be sent to when they run /hub, or when a fallback      #",
            "# | occurs. If the parent family is unavailable, the       #",
            "# | root family is used instead.                           #",
            "#                                                          #",
            "#   NOTE: If this value is set for the root family         #",
            "#         it will be ignored.                              #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(1)
    public String parentFamily = "";

    @Node(2)
    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                      Load Balancing                      #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "# | Load balancing is the system through which networks    #",
            "# | manage player influxes by spreading out players        #",
            "# | across various server nodes.                           #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    public String loadBalancer = "default";

    @Node(3)
    @Comment({
            "#",
            "# Provide additional metadata for the family.",
            "# Metadata provided here is non-essential, meaning that RustyConnector is capable of running without anything provided here.",
            "# Ensure that the provided metadata conforms to valid JSON syntax.",
            "#"
    })
    public String metadata = "{\\\"serverSoftCap\\\": 30, \\\"serverHardCap\\\": 40}";

    public static ScalarFamilyConfig New(String familyID) throws IOException {
        Printer printer = new Printer()
                .pathReplacements(Map.of("id", familyID))
                .commentReplacements(Map.of("id", familyID));
        return DeclarativeYAML.From(ScalarFamilyConfig.class, printer);
    }
}