package group.aelysium.rustyconnector.plugin.velocity.config;

import group.aelysium.ara.Particle;
import group.aelysium.declarative_yaml.DeclarativeYAML;
import group.aelysium.declarative_yaml.annotations.*;
import group.aelysium.declarative_yaml.lib.Printer;
import group.aelysium.rustyconnector.proxy.family.load_balancing.LoadBalancer;
import group.aelysium.rustyconnector.proxy.family.load_balancing.LoadBalancerAlgorithmExchange;
import group.aelysium.rustyconnector.proxy.util.LiquidTimestamp;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

@Config("plugins/rustyconnector/load_balancers/{name}.yml")
@Git(value = "rustyconnector", required = false)
@Comment({
        "############################################################",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "#                      Load Balancing                      #",
        "#                                                          #",
        "#               ---------------------------                #",
        "# | Load balancing is the system through which networks    #",
        "# | manage player influxes by spreading out players        #",
        "# | across various server nodes.                           #",
        "#               ---------------------------                #",
        "#                                                          #",
        "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
        "############################################################"
})
public class LoadBalancerConfig {
    @PathParameter("name")
    private String name;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                        Algorithm                         #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | Depending on your needs, you might want to balance     #",
            "# | player influxes in various ways.                       #",
            "# | RustyConnector currently supports the following        #",
            "# | balancing algorithms, if there are other algorithms    #",
            "# | you'd like to see in the future, let us know!          #",
            "#                                                          #",
            "#  ⚫ LEAST_CONNECTION -                                   #",
            "#             Connects players to the server with the      #",
            "#             the fewest players currently connected.      #",
            "#             This mode is best if you want evenly         #",
            "#             distributed players across all servers.      #",
            "#  ⚫ MOST_CONNECTION -                                    #",
            "#             Connects players to the server with the      #",
            "#             the most players currently connected.        #",
            "#             This mode is best if you want to fill        #",
            "#             servers up as quickly as possible.           #",
            "#  ⚫ ROUND_ROBIN -                                        #",
            "#             Every time a connection occurs, the next     #",
            "#             server in the load balancer will be queued   #",
            "#             for the next connection.                     #",
            "#             Once the load balancer reaches the end of    #",
            "#             the server queue, the load balancer will     #",
            "#             loop back to the beginning and start again.  #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node()
    private String algorithm = "ROUND_ROBIN";

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                         Weighted                         #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | If set to `true` the load balancer will attempt to     #",
            "# | put players into the servers with the highest `weight` #",
            "#                                                          #",
            "# | `weight` is defined in your individual server configs. #",
            "#                                                          #",
            "# | If multiple servers are set to be the same weight      #",
            "# | level, the load balancer will use `algorithm` on those #",
            "# | servers until they have been filled. It will then step #",
            "# | to the next, lower, weight level and continue.         #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################",
            "#",
            "# If set to `true`. the load balancer will attempt to put players into the servers with the highest `weight`",
            "# `weight` is defined in the individual server configs on RustyConnector-paper.",
            "#",
            "# If multiple server are set to be the same weight level, the load balancer will use `algorithm` on those servers",
            "# until they have been filled. It will then step to the next, lower, weight level and continue.",
            "#"
    })
    @Node(1)
    private boolean weighted = false;

    @Comment({
            "############################################################",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "#                       Persistence                        #",
            "#                                                          #",
            "#               ---------------------------                #",
            "# | Persistence defines whether the load balancer          #",
            "# | should give up if it's first attempt to connect a      #",
            "# | player to this family fails.                           #",
            "# | If this is off, the player will have to manually       #",
            "# | try again if the attempt fails.                        #",
            "# | If this is on, the load balancer will keep trying      #",
            "# | until it's number of attempts has exceeded `attempts`  #",
            "#               ---------------------------                #",
            "#                                                          #",
            "#||||||||||||||||||||||||||||||||||||||||||||||||||||||||||#",
            "############################################################"
    })
    @Node(2)
    private boolean persistence_enabled = false;

    @Comment({
            "#",
            "# This is how many times the load balancer will attempt to connect a player to this family before giving up.",
            "# If you have lots of servers in this family with whitelists, it might be better to increase this value.",
            "#",
            "# Set to -1 for the load balancer to never give up. (In most cases this isn't really the best idea)",
            "#"
    })
    @Node(3)
    private int persistence_attempts = 5;

    @Comment({
            "#",
            "# The interval by which this load balancer will re-balance itself..",
            "# Re-balancing operations are expensive so this should be set to a reasonable",
            "# value based on how many servers this load balancer will be managing.",
            "# Something like 15 Seconds is a good default to leave.",
            "#",
            "# This value is a Liquid Timestamp. Check the Wiki for more details.",
            "#",
    })
    @Node(4)
    private String rebalance = "15 SECONDS";

    public LoadBalancer.Tinder<?> tinder() throws ParseException {
        return LoadBalancerAlgorithmExchange.generateTinder(this.algorithm, new LoadBalancerAlgorithmExchange.Settings(
                this.weighted,
                this.persistence_enabled,
                this.persistence_attempts,
                LiquidTimestamp.from(rebalance)
        ));
    }

    public static LoadBalancerConfig New(String name) throws IOException {
        Printer printer = new Printer()
                .pathReplacements(Map.of("name", name));
        return DeclarativeYAML.load(LoadBalancerConfig.class, printer);
    }
}
