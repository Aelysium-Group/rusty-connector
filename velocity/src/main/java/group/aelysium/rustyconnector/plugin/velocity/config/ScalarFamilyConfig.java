package group.aelysium.rustyconnector.plugin.velocity.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.declarative_yaml.lib.Printer;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.proxy.family.scalar_family.ScalarFamily;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@Config("plugins/rustyconnector/scalar_families/{id}.yml")
@Git(value = "rustyconnector", required = false)
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
    private String id;

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
    private String displayName = "";

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
    private String parentFamily = "";

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
    private String loadBalancer = "default";

    @Node(3)
    @Comment({
            "#",
            "# Provide additional metadata for the family.",
            "# Metadata provided here is non-essential, meaning that RustyConnector is capable of running without anything provided here.",
            "# Ensure that the provided metadata conforms to valid JSON syntax.",
            "#",
            "# For built-in metadata options, check the Aelysium wiki:",
            "# https://wiki.aelysium.group/rusty-connector/docs/concepts/metadata/",
            "#"
    })
    private String metadata = "{\"serverSoftCap\": 30, \"serverHardCap\": 40}";

    public ScalarFamily.Tinder tinder() throws IOException, ParseException {
        ScalarFamily.Tinder tinder = new ScalarFamily.Tinder(
                id,
                displayName.isEmpty() ? null : displayName,
                parentFamily.isEmpty() ? null : parentFamily,
                LoadBalancerConfig.New(loadBalancer).tinder()
        );

        Gson gson = new Gson();
        JsonObject metadataJson = gson.fromJson(this.metadata, JsonObject.class);
        metadataJson.entrySet().forEach(e->tinder.metadata(e.getKey(), Packet.Parameter.fromJSON(e.getValue()).getOriginalValue()));

        return tinder;
    }

    public static ScalarFamilyConfig New(String familyID) throws IOException {
        Printer printer = new Printer()
                .pathReplacements(Map.of("id", familyID))
                .commentReplacements(Map.of("id", familyID));
        return DeclarativeYAML.load(ScalarFamilyConfig.class, printer);
    }
}