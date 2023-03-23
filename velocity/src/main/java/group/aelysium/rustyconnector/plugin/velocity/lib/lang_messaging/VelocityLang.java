package group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging;

import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.hash.MD5;
import group.aelysium.rustyconnector.core.lib.lang_messaging.ASCIIAlphabet;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.VelocityRustyConnector;
import group.aelysium.rustyconnector.plugin.velocity.lib.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.load_balancing.PaperServerLoadBalancer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.PaperServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.ServerFamily;
import net.kyori.adventure.text.Component;

import java.util.Date;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface VelocityLang extends Lang {

    Message WORDMARK_REGISTERED_FAMILIES = () -> // font: ANSI Shadow
            join(
                    Lang.newlines(),
                    ASCIIAlphabet.generate("registered"),
                    SPACING,
                    ASCIIAlphabet.generate("families")
            );

    Message RC_ROOT_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            text("Blue commands will return information or data to you! They will not cause changes to be made.",GRAY),
            text("Orange commands will make the plugin do something. Make sure you know what these commands do before using them!",GRAY),
            SPACING,
            BORDER,
            SPACING,
            text("/rc family", AQUA),
            text("View family related information.", DARK_GRAY),
            SPACING,
            text("/rc message", AQUA),
            text("Access recently sent rusty-connector messages.", DARK_GRAY),
            SPACING,
            text("/rc register", AQUA),
            text("See server registration options.", DARK_GRAY),
            SPACING,
            text("/rc reload", AQUA),
            text("See reload options.", DARK_GRAY),
            SPACING,
            BORDER
    );

    Message RC_SERVER_COMMAND_CLIENT = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            text("The /server command is not supported on this network!",RED),
            SPACING,
            BORDER
    );
    Message RC_SERVER_COMMAND_CONSOLE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            text("The /server command is not supported on this network!",RED),
            SPACING,
            BORDER
    );

    Message RC_MESSAGE_ROOT_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc message get <Message ID>", AQUA),
            text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", DARK_GRAY),
            SPACING,
            text("/rc message list <page number>", AQUA),
            text("Lists all currently cached messages! As new messages get cached, older ones will be pushed out of the cache.", DARK_GRAY),
            SPACING,
            BORDER
    );

    Message RC_MESSAGE_GET_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc message get <Message ID>",AQUA),
            text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", GRAY),
            SPACING,
            BORDER
    );

    Message RC_RELOAD_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            text("Blue commands will return information or data to you! They will not cause changes to be made.",GRAY),
            text("Orange commands will make the plugin do something. Make sure you know what these commands do before using them!",GRAY),
            SPACING,
            text("Using reload to create or delete families is not currently supported. You must restart your proxy to add or remove families.",RED),
            SPACING,
            BORDER,
            SPACING,
            text("/rc reload proxy", GOLD),
            text("Reloads config.yml", DARK_GRAY),
            text("Does NOT reload families.", RED),
            text("Does NOT reload redis.", RED),
            SPACING,
            text("/rc reload family <family name>", GOLD),
            text("Reload a specific family's configuration.", DARK_GRAY),
            text("All servers will have to re-register into this family.", RED),
            SPACING,
            text("/rc reload logger", GOLD),
            text("Reloads logger.yml", DARK_GRAY),
            SPACING,
            text("/rc reload whitelists", GOLD),
            text("Reloads all active whitelists. Will also register if the proxy or a family's whitelist settings change.", DARK_GRAY),
            text("Players already connected to servers will NOT be kicked out if a whitelist is activated.", RED),
            SPACING,
            BORDER
    );

    Message RC_REGISTER_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            text("Blue commands will return information or data to you! They will not cause changes to be made.",GRAY),
            text("Orange commands will make the plugin do something. Make sure you know what these commands do before using them!",GRAY),
            SPACING,
            BORDER,
            SPACING,
            text("/rc register all", GOLD),
            text("Register all servers to the proxy.", DARK_GRAY),
            SPACING,
            text("/rc register family <family name>", GOLD),
            text("Register all servers associate with a specific family.", DARK_GRAY),
            SPACING,
            BORDER
    );

    ParameterizedMessage3<Long, Date, String> RC_MESSAGE_GET_MESSAGE = (id, date, contents) -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.build().color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("ID: " + id, GRAY),
            text("Date: " + date, GRAY),
            text("Contents: " + contents, GRAY),
            SPACING,
            BORDER
    );

    ParameterizedMessage1<String> RC_MESSAGE_ERROR = error -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.build().color(RED),
            SPACING,
            BORDER,
            SPACING,
            text(error,GRAY),
            SPACING,
            BORDER
    );

    Message RC_FAMILY = () -> {

        Component families = text("");
        for (ServerFamily<? extends PaperServerLoadBalancer> family : VelocityRustyConnector.getInstance().getVirtualServer().getFamilyManager().dump()) {
            families = families.append(text("[ "+family.getName()+" ] "));
        }

        return join(
                Lang.newlines(),
                BORDER,
                SPACING,
                WORDMARK_REGISTERED_FAMILIES.build().color(AQUA),
                SPACING,
                BORDER,
                SPACING,
                families.color(GOLD),
                SPACING,
                BORDER,
                SPACING,
                text("To see more details about a particular family use:", GRAY),
                text("/rc family <family name>",DARK_AQUA),
                SPACING,
                BORDER
        );
    };

    ParameterizedMessage1<String> RC_FAMILY_ERROR = error -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_REGISTERED_FAMILIES.build().color(RED),
            SPACING,
            BORDER,
            SPACING,
            text(error,GRAY),
            SPACING,
            BORDER
    );

    ParameterizedMessage1<String> RC_REGISTER_ERROR = error -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            ASCIIAlphabet.generate("REGISTER", RED),
            SPACING,
            BORDER,
            SPACING,
            text(error,GRAY),
            SPACING,
            BORDER
    );

    ParameterizedMessage1<ServerFamily<? extends PaperServerLoadBalancer>> RC_FAMILY_INFO = (family) -> {

        Component servers = text("");
        int i = 0;

        if(family.getRegisteredServers() == null) servers = text("There are no registered servers.", DARK_GRAY);
        else if(family.getRegisteredServers().size() == 0) servers = text("There are no registered servers.", DARK_GRAY);
            else for (PaperServer server : family.getRegisteredServers()) {
                if(family.getLoadBalancer().getIndex() == i)
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.getRegisteredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.getRegisteredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.getPlayerCount()+" ("+server.getSoftPlayerCap()+" <> "+server.getHardPlayerCap()+") w-"+server.getWeight()+"] <<<<<"
                                    , GREEN));
                else
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.getRegisteredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.getRegisteredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.getPlayerCount()+" ("+server.getSoftPlayerCap()+" <> "+server.getHardPlayerCap()+") w-"+server.getWeight()+"]"
                                    , GRAY));

                servers = servers.append(newline());

                i++;
            }


        return join(
                Lang.newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.getName(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                text("   ---| Online Players: "+family.getPlayerCount()),
                text("   ---| Registered Servers: "+family.serverCount()),
                text("   ---| Load Balancing:"),
                text("      | - Algorithm: "+family.getLoadBalancer()),
                text("      | - Weighted Sorting: "+family.isWeighted()),
                text("      | - Persistence: "+family.getLoadBalancer().isPersistent()),
                text("      | - Max Attempts: "+family.getLoadBalancer().getAttempts()),
                SPACING,
                BORDER,
                SPACING,
                text("Registered Servers", AQUA),
                SPACING,
                text("/rc family <family name> sort", GOLD),
                text("Will cause the family to completely resort itself in accordance with it's load balancing algorithm.", DARK_GRAY),
                SPACING,
                text("/rc family <family name> resetIndex", GOLD),
                text("Will reset the family's input to the first server in the family.", DARK_GRAY),
                SPACING,
                servers,
                SPACING,
                BORDER
        );
    };


    Component TPA_NO_PERMISSION = text("You do not have permission to use this command.",RED);

    Message TPA_USAGE = () -> join(
            Lang.newlines(),
            text("Usage: /tpa <<username>, deny, accept>",RED)
    );
    Message TPA_DENY_USAGE = () -> join(
            Lang.newlines(),
            text("Usage: /tpa deny <username>",RED),
            text("Deny a tpa request from a user.",GRAY)
    );
    Message TPA_ACCEPT_USAGE = () -> join(
            Lang.newlines(),
            text("Usage: /tpa accept <username>",RED),
            text("Accept a tpa request from a user.",GRAY)
    );

    ParameterizedMessage1<String> TPA_FAILURE = username -> join(
            Lang.newlines(),
            text("Unable to tpa to "+username+"!",RED)
    );
    ParameterizedMessage1<String> TPA_FAILURE_TARGET = username -> join(
            Lang.newlines(),
            text("Unable to tpa "+username+" to you!",RED)
    );
    Component TPA_FAILURE_SELF_TP = text("You can't teleport to yourself!",RED);
    ParameterizedMessage1<String> TPA_FAILURE_NO_USERNAME = username -> join(
            Lang.newlines(),
            text(username+" isn't online!",RED)
    );
    ParameterizedMessage1<String> TPA_FAILURE_NO_REQUEST = username -> join(
            Lang.newlines(),
            text(username+" hasn't sent you any recent tpa requests!",RED)
    );
    ParameterizedMessage1<String> TPA_REQUEST_DUPLICATE = username -> join(
            Lang.newlines(),
            text("You already have a pending tpa request to "+ username +"!",RED)
    );
    ParameterizedMessage1<String> TPA_REQUEST_QUERY = username -> join(
            Lang.newlines(),
            text(username + " has requested to teleport to you!",GOLD),
            text("Use `",GRAY).append(Component.text("/tpa accept "+username,AQUA)).append(Component.text("` to accept!", GRAY)),
            text("Use `",GRAY).append(Component.text("/tpa deny "+username,AQUA)).append(Component.text("` to deny!", GRAY))
    );
    ParameterizedMessage1<String> TPA_REQUEST_SUBMISSION = username -> join(
            Lang.newlines(),
            text("You requested to teleport to "+ username +"!",GREEN)
    );
    ParameterizedMessage1<String> TPA_REQUEST_ACCEPTED_SENDER = username -> join(
            Lang.newlines(),
            text(username +" accepted your request!",GREEN),
            text("Attempting to teleport...",GRAY)
    );
    ParameterizedMessage1<String> TPA_REQUEST_ACCEPTED_TARGET = username -> join(
            Lang.newlines(),
            text(username +"'s tpa request has been accepted!",GREEN),
            text("Attempting to teleport...",GRAY)
    );
    ParameterizedMessage1<String> TPA_REQUEST_DENIED_SENDER = username -> join(
            Lang.newlines(),
            text(username +" denied your request!",RED)
    );
    ParameterizedMessage1<String> TPA_REQUEST_DENIED_TARGET = username -> join(
            Lang.newlines(),
            text(username +"'s tpa request has been denied!",RED),
            text("They've been notified...",GRAY)
    );
    ParameterizedMessage1<String> TPA_REQUEST_EXPIRED = username -> join(
            Lang.newlines(),
            text("Your tpa request to "+username+" has expired!",RED)
    );


    Message PRIVATE_KEY = () -> join(
            Lang.newlines(),
            SPACING,
            BORDER,
            SPACING,
            text("No private-key was defined! Generating one now...", RED),
            text("Paste the key below into the `private-key` field in `config.yml`! Then restart your proxy.", RED),
            SPACING,
            BORDER,
            text(MD5.generatePrivateKey(),YELLOW),
            BORDER,
            SPACING
    );

    ParameterizedMessage1<ServerInfo> PONG = serverInfo -> text(
            "Proxy" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_pong() +" " +
                    "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
    );

    ParameterizedMessage1<ServerInfo> PONG_CANCELED = serverInfo -> text(
            "Proxy" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_canceledRequest() +" " +
                    "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
    );

    ParameterizedMessage1<ServerInfo> PING = serverInfo -> text(
            "Proxy" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_ping() +" " +
                    "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
    );

    ParameterizedMessage2<ServerInfo, String> REGISTRATION_REQUEST = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_requestingRegistration() +" "+familyName
    );

    ParameterizedMessage2<ServerInfo, String> REGISTERED = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_registered() +" "+familyName
    );

    ParameterizedMessage2<ServerInfo, String> REGISTRATION_CANCELED = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_canceledRequest() +" "+familyName
    );

    ParameterizedMessage2<ServerInfo, String> UNREGISTRATION_REQUEST = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_requestingUnregistration() +" "+familyName
    );

    ParameterizedMessage2<ServerInfo, String> UNREGISTERED = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_unregistered() +" "+familyName
    );

    ParameterizedMessage2<ServerInfo, String> UNREGISTRATION_CANCELED = (server, familyName) -> text(
            "["+server.getName()+"]" +
                    "("+server.getAddress().getHostName()+":"+server.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_canceledRequest() +" "+familyName
    );

    Message CALL_FOR_REGISTRATION = () -> text("[Velocity](127.0.0.1) " + LoggerConfig.getConfig().getConsoleIcons_callForRegistration() +" EVERYONE");
    ParameterizedMessage1<String> CALL_FOR_FAMILY_REGISTRATION = (familyName) -> text("[Velocity](127.0.0.1) " + LoggerConfig.getConfig().getConsoleIcons_callForRegistration() +" "+ familyName);

    ParameterizedMessage1<ServerFamily<? extends PaperServerLoadBalancer>> FAMILY_BALANCING = family -> text(
            family.getName() + " " + LoggerConfig.getConfig().getConsoleIcons_familyBalancing()
    );
}
