package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.rustyconnector.common.config.*;
import group.aelysium.rustyconnector.proxy.family.whitelist.Whitelist;

import java.io.IOException;
import java.util.List;

@Config("whitelist/{name}.yml")
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                        Whitelist                         #",
        "#                                                          #",
        "#               ---------------------------                #",
        "# | Setup your whitelist! The name of this whitelist       #",
        "# | is the same as the name you give this file!            #",
        "#                                                          #",
        "# | To make a new whitelist, just duplicate this           #",
        "# | template, rename it, and configure it how you'd like!  #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################"
})
public class WhitelistConfig {
    @PathParameter("name")
    private String name;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                         Players                          #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | The players whitelist allows three parameters to give  #",
            "# | your criteria more or less flexibility!                #",
            "# | Username, uuid, and IP Address                         #",
            "#                                                          #",
            "# | You are free to use all three or only one of these     #",
            "# | criteria to check if someone is whitelisted!           #",
            "# | As a bear minimum, you MUST at least include username. #",
            "#                                                          #",
            "# NOTE: It's not hard for any good hacker to spoof an      #",
            "#       IP Address. As such, make sure you are properly    #",
            "#       validating your connections and not using only IP  #",
            "#       Addresses as a whitelist criteria!                 #",
            "# NOTE: Make sure that if you are working with player      #",
            "#       IP Addresses that you properly inform your members #",
            "#       and that you never leak that information!          #",
            "#                                                          #",
            "#       There are laws that protect user data!             #",
            "#       Don't break them!                                  #",
            "#                                                          #",
            "# NOTE: You'll want to make sure that you use a UUID       #",
            "#       format containing dashes! If you use the format    #",
            "#       without dashes the whitelist will fail to load!    #",
            "#                                                          #",
            "#       Example:                                           #",
            "#       00000000-0000-0000-0000-000000000000               #",
            "#                                                          #",
            "#       Example (INVALID):                                 #",
            "#       00000000000000000000000000000000                   #",
            "#                                                          #",
            "#                                                          #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(order = 0, key = "use-players", defaultValue = "false")
    private boolean usePlayers;

    @Node(order = 1, key = "players", defaultValue = "[]")
    private List<Whitelist.Filter> players;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                       Permission                         #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | If you'd only like players with a certain permission   #",
            "# | to join this family, enable this value!                #",
            "#                                                          #",
            "#   rustyconnector.whitelist.<whitelist name>              #",
            "# | Gives a player permission to pass the                  #",
            "# | specific whitelist.                                    #",
            "#                                                          #",
            "#   rustyconnector.whitelist.*                             #",
            "# | Gives a player permission to pass all whitelists.      #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(order = 2, key = "use-permission", defaultValue = "false")
    private boolean usePermission;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                      Kick Message                        #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | The message to show a player if they                   #",
            "# | fail the whitelist.                                    #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(order = 3, key = "message", defaultValue = "You aren't whitelisted on this server!")
    private String message;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                         Strict                           #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | Set if the whitelist is strict or not.                 #",
            "#                                                          #",
            "# | If `true` then a player must match all                 #",
            "# | activated filters.                                     #",
            "#                                                          #",
            "# | If `false` then a player must only match one of the    #",
            "# | activated filters.                                     #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(order = 4, key = "strict", defaultValue = "false")
    private boolean strict;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                        Inverted                          #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | Inverting a whitelist will cause it to operate as      #",
            "# | a blacklist.                                           #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(order = 5, key = "inverted", defaultValue = "false")
    private boolean inverted;

    public Whitelist.Tinder tinder() {
        Whitelist.Settings settings = new Whitelist.Settings(
                this.name,
                this.usePlayers,
                this.players,
                this.usePermission,
                this.message,
                this.strict,
                this.inverted
        );
        return new Whitelist.Tinder(settings);
    }

    public static WhitelistConfig New(String whitelistName) throws IOException {
        return ConfigLoader.load(WhitelistConfig.class, whitelistName);
    }
}
