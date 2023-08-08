package group.aelysium.rustyconnector.plugin.velocity.lib.lang_messaging;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.lang_messaging.ASCIIAlphabet;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.model.LiquidTimestamp;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.central.VelocityAPI;
import group.aelysium.rustyconnector.plugin.velocity.config.LoggerConfig;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.RootServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ScalarServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.StaticServerFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.parties.Party;
import group.aelysium.rustyconnector.plugin.velocity.lib.players.FakePlayer;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PlayerServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.bases.BaseServerFamily;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.Objects;
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
            text("/rc reload", AQUA),
            text("See reload options.", DARK_GRAY),
            SPACING,
            text("/rc send", AQUA),
            text("Send players from families and servers to other families or servers.", DARK_GRAY),
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

    Message RC_SEND_USAGE = () -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build().color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc send <username> <family name>", GOLD),
            text("Sends a player from one family to another!", DARK_GRAY),
            BORDER,
            SPACING,
            text("/rc send server <username> <server name>", GOLD),
            text("Force a player to connect to a specific server on the proxy. This bypasses player caps and family whitelists.", DARK_GRAY),
            text("If you have multiple servers with the same name, this feature may send players to a server other than the one you intended.", DARK_GRAY),
            SPACING,
            BORDER
    );
    ParameterizedMessage1<String> RC_SEND_NO_PLAYER = username -> join(
            Lang.newlines(),
            text("There is no online player with the username: "+username+"!", RED)
    );
    ParameterizedMessage1<String> RC_SEND_NO_FAMILY = familyName -> join(
            Lang.newlines(),
            text("There is no family with the name: "+familyName+"!", RED)
    );
    ParameterizedMessage1<String> RC_SEND_NO_SERVER = serverName -> join(
            Lang.newlines(),
            text("There is no server with the name: "+serverName+"!", RED)
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

    ParameterizedMessage1<CacheableMessage> RC_MESSAGE_GET_MESSAGE = (message) -> join(
            Lang.newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.build().color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("Status: " + message.getSentence().name(), message.getSentence().color()),
            text("Reason: " + message.getSentenceReason(), message.getSentence().color()),
            SPACING,
            text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
            text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
            text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
            SPACING
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
        VelocityAPI api = VelocityAPI.get();
        Component families = text("");
        for (BaseServerFamily family : api.services().familyService().dump()) {
            if(family instanceof ScalarServerFamily)
                families = families.append(text("[ "+family.name()+" ] ").color(GOLD));
            if(family instanceof StaticServerFamily)
                families = families.append(text("[ "+family.name()+" ] ").color(DARK_GREEN));
        }

        return join(
                Lang.newlines(),
                BORDER,
                SPACING,
                WORDMARK_REGISTERED_FAMILIES.build().color(AQUA),
                SPACING,
                BORDER,
                SPACING,
                text("Gold families are Scalar. Green families are Static.", GRAY),
                families,
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

    ParameterizedMessage1<ScalarServerFamily> RC_SCALAR_FAMILY_INFO = (family) -> {
        Component servers = text("");
        int i = 0;

        if(family.registeredServers() == null) servers = text("There are no registered servers.", DARK_GRAY);
        else if(family.registeredServers().size() == 0) servers = text("There are no registered servers.", DARK_GRAY);
        else for (PlayerServer server : family.registeredServers()) {
                if(family.loadBalancer().index() == i)
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.registeredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.playerCount()+" ("+server.softPlayerCap()+" <> "+server.hardPlayerCap()+") w-"+server.weight()+"] <<<<<"
                                    , GREEN));
                else
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.registeredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.playerCount()+" ("+server.softPlayerCap()+" <> "+server.hardPlayerCap()+") w-"+server.weight()+"]"
                                    , GRAY));

                servers = servers.append(newline());

                i++;
            }

        RootServerFamily rootFamily = VelocityAPI.get().services().familyService().rootFamily();
        String parentFamilyName = rootFamily.name();
        try {
            parentFamilyName = Objects.requireNonNull(family.parent().get()).name();
        } catch (Exception ignore) {}
        if(family.equals(rootFamily)) parentFamilyName = "none";

        return join(
                Lang.newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.name(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                text("   ---| Online Players: "+family.playerCount()),
                text("   ---| Registered Servers: "+family.serverCount()),
                text("   ---| Parent Family: "+ parentFamilyName),
                text("   ---| Load Balancing:"),
                text("      | - Algorithm: "+family.loadBalancer()),
                text("      | - Weighted Sorting: "+family.isWeighted()),
                text("      | - Persistence: "+family.loadBalancer().persistent()),
                text("      | - Max Attempts: "+family.loadBalancer().attempts()),
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

    ParameterizedMessage1<StaticServerFamily> RC_STATIC_FAMILY_INFO = (family) -> {
        Component servers = text("");
        int i = 0;

        if(family.registeredServers() == null) servers = text("There are no registered servers.", DARK_GRAY);
        else if(family.registeredServers().size() == 0) servers = text("There are no registered servers.", DARK_GRAY);
        else for (PlayerServer server : family.registeredServers()) {
                if(family.loadBalancer().index() == i)
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.registeredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.playerCount()+" ("+server.softPlayerCap()+" <> "+server.hardPlayerCap()+") w-"+server.weight()+"] <<<<<"
                                    , GREEN));
                else
                    servers = servers.append(
                            text("   ---| "+(i + 1)+". ["+server.registeredServer().getServerInfo().getName()+"]" +
                                            "("+ AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress()) +") " +
                                            "["+server.playerCount()+" ("+server.softPlayerCap()+" <> "+server.hardPlayerCap()+") w-"+server.weight()+"]"
                                    , GRAY));

                servers = servers.append(newline());

                i++;
            }

        RootServerFamily rootFamily = VelocityAPI.get().services().familyService().rootFamily();
        String parentFamilyName = rootFamily.name();
        try {
            parentFamilyName = Objects.requireNonNull(family.parent().get()).name();
        } catch (Exception ignore) {}
        if(family.equals(rootFamily)) parentFamilyName = "none";

        LiquidTimestamp expiration = family.homeServerExpiration();
        String homeServerExpiration = "NEVER";
        if(expiration != null) homeServerExpiration = expiration.toString();

        return join(
                Lang.newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.name(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                text("   ---| Online Players: "+family.playerCount()),
                text("   ---| Registered Servers: "+family.serverCount()),
                text("   ---| Parent Family: "+ parentFamilyName),
                text("   ---| Home Server Expiration: "+homeServerExpiration),
                text("   ---| Load Balancing:"),
                text("      | - Algorithm: "+family.loadBalancer()),
                text("      | - Weighted Sorting: "+family.isWeighted()),
                text("      | - Persistence: "+family.loadBalancer().persistent()),
                text("      | - Max Attempts: "+family.loadBalancer().attempts()),
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

    Component MISSING_HOME_SERVER = text("The server you were meant to be connected to is unavailable! In the meantime you've been connected to a fallback server!", RED);
    Component BLOCKED_STATIC_FAMILY_JOIN_ATTEMPT = text("The server you were meant to be connected to is unavailable! Please try again later!", RED);


    Component COMMAND_NO_PERMISSION = text("You do not have permission to use this command.",RED);

    Message TPA_USAGE = () -> text("Usage: /tpa <<username>, deny, accept>",RED);
    Message TPA_IGNORE_USAGE = () -> join(
            Lang.newlines(),
            text("Usage: /tpa ignore <username>",RED),
            text("Deny a tpa request from a user.",GRAY)
    );
    Message TPA_ACCEPT_USAGE = () -> join(
            Lang.newlines(),
            text("Usage: /tpa accept <username>",RED),
            text("Accept a tpa request from a user.",GRAY)
    );

    ParameterizedMessage1<String> TPA_FAILURE = username -> text("Unable to tpa to "+username+"!",RED);
    ParameterizedMessage1<String> TPA_FAILURE_TARGET = username -> text("Unable to tpa "+username+" to you!",RED);
    Component TPA_FAILURE_SELF_TP = text("You can't teleport to yourself!",RED);
    ParameterizedMessage1<String> TPA_FAILURE_NO_USERNAME = username -> text(username+" isn't online!",RED);
    ParameterizedMessage1<String> TPA_FAILURE_NO_REQUEST = username -> text(username+" hasn't sent you any recent tpa requests!",RED);
    ParameterizedMessage1<String> TPA_REQUEST_DUPLICATE = username -> text("You already have a pending tpa request to "+ username +"!",RED);

    ParameterizedMessage1<Player> TPA_REQUEST_QUERY = (sender) -> join(
            Lang.newlines(),
            text("Hey! " + sender.getUsername() + " has requested to teleport to you!",GOLD),
            join(
                    JoinConfiguration.separator(space()),
                    text("[Accept]", GREEN).hoverEvent(HoverEvent.showText(text("Let "+sender.getUsername()+" teleport to you"))).clickEvent(ClickEvent.runCommand("/tpa accept "+sender.getUsername())),
                    text("[Ignore]", RED).hoverEvent(HoverEvent.showText(text("Ignore "+sender.getUsername()+"'s request"))).clickEvent(ClickEvent.runCommand("/party ignore "+sender.getUsername()))
            )
    );
    ParameterizedMessage1<String> TPA_REQUEST_SUBMISSION = username -> text("You requested to teleport to "+ username +"!",GREEN);
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
    ParameterizedMessage1<String> TPA_REQUEST_DENIED_SENDER = username -> text(username +" denied your request!",RED);
    ParameterizedMessage1<String> TPA_REQUEST_DENIED_TARGET = username -> join(
            Lang.newlines(),
            text(username +"'s tpa request has been denied!",RED),
            text("They've been notified...",GRAY)
    );
    ParameterizedMessage1<String> TPA_REQUEST_EXPIRED = username -> text("Your tpa request to "+username+" has expired!",RED);

    ParameterizedMessage2<Party, Player> PARTY_BOARD = (party, member) -> {
        boolean hasParty = party != null;

        if(hasParty) {
            boolean isLeader = party.leader().equals(member);
            boolean canInvite;
            try {
                boolean onlyLeaderCanInvite = VelocityAPI.get().services().partyService().orElseThrow().settings().onlyLeaderCanInvite();
                canInvite = !onlyLeaderCanInvite || isLeader;
            } catch (Exception ignore) {
                canInvite = isLeader;
            }

            final Component[] playersList = {text("")};
            if(isLeader)
                party.players().forEach(partyMember -> {

                    playersList[0] = playersList[0].appendNewline();

                    if(party.leader().equals(partyMember))
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text("[x]", RED).hoverEvent(HoverEvent.showText(text("Leave Party"))).clickEvent(ClickEvent.runCommand("/party leave")),
                                        text("[^]", GRAY),
                                        text(partyMember.getUsername(), WHITE),
                                        text("[Leader]", BLUE)
                                )
                        );
                    else
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text("[x]", RED).hoverEvent(HoverEvent.showText(text("Kick Player"))).clickEvent(ClickEvent.runCommand("/party kick " + partyMember.getUsername())),
                                        text("[^]", GREEN).hoverEvent(HoverEvent.showText(text("Promote to Leader"))).clickEvent(ClickEvent.runCommand("/party promote " + partyMember.getUsername())),
                                        text(partyMember.getUsername(), WHITE)
                                )
                        );
                });
            else
                party.players().forEach(partyMember -> {
                    playersList[0] = playersList[0].appendNewline();
                    if(party.leader().equals(partyMember))
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text(partyMember.getUsername(), WHITE),
                                        text("[Leader]", BLUE)
                                )
                        );
                    else
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text(partyMember.getUsername(), WHITE)
                                )
                        );
                });

            Component header;
            if(canInvite)
                header = text("-------------------", GRAY)
                 .append(text(" Party ", WHITE))
                 .append(text("[+]", GREEN)).hoverEvent(HoverEvent.showText(text("Invite Player"))).clickEvent(ClickEvent.suggestCommand("/party invite <username>"))
                 .append(text(" ------------------", GRAY));
            else
                header = text("------------------", GRAY)
                        .append(text(" Party ", WHITE))
                        .append(text(" ------------------", GRAY));

            if(isLeader)
                return join(
                        Lang.newlines(),
                        header,
                        playersList[0],
                        space(),
                        text("----------------", GRAY)
                                .appendSpace()
                                .append(text("Disband", RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party disband")))
                                .appendSpace()
                                .appendSpace()
                                .append(text("Leave", RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party leave")))
                                .appendSpace()
                                .append(text("----------------", GRAY))
                );
            else
                return join(
                        Lang.newlines(),
                        header,
                        playersList[0],
                        space(),
                        text("------------------ ", GRAY).append(text("Leave", RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party leave"))).append(text(" ------------------", GRAY))
                        );
        }

        return text("Click here to create a party.", YELLOW, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party create"));
    };

    ParameterizedMessage1<Player> PARTY_INVITE_RECEIVED = (sender) -> join(
            Lang.newlines(),
            text("Hey! "+ sender.getUsername() +" wants you to join their party!", NamedTextColor.GRAY),
            join(
                    JoinConfiguration.separator(space()),
                    text("[Accept]", GREEN).hoverEvent(HoverEvent.showText(text("Accept party invite"))).clickEvent(ClickEvent.runCommand("/party invites "+sender.getUsername()+" accept")),
                    text("[Ignore]", RED).hoverEvent(HoverEvent.showText(text("Ignore party invite"))).clickEvent(ClickEvent.runCommand("/party invites "+sender.getUsername()+" ignore"))
            )
    );

    Message PARTY_USAGE_INVITES = () -> text("Usage: /party invites <username> <accept / ignore>",RED);

    Message PARTY_USAGE_INVITE = () -> text("Usage: /party invite <username>",RED);

    Message PARTY_USAGE_KICK = () -> text("Usage: /party kick <username>",RED);

    Message PARTY_USAGE_PROMOTE = () -> text("Usage: /party promote <username>",RED);

    Message PARTY_DISBANDED = () -> text("Your party has been disbanded.",GRAY);

    ParameterizedMessage1<Player> FRIENDS_BOARD = (player) -> {
        VelocityAPI api = VelocityAPI.get();
        FriendsService friendsService = api.services().friendsService().orElseThrow();
        int maxFriends = friendsService.settings().maxFriends();
        player.sendMessage(text("Getting friends...", GRAY));

        boolean isPartyEnabled = false;
        try {
            api.services().partyService().orElseThrow();
            isPartyEnabled = true;
        } catch (Exception ignore) {}
        boolean finalIsPartyEnabled = isPartyEnabled;

        boolean isFriendMessagingEnabled = friendsService.settings().allowMessaging();
        boolean canSeeFriendFamilies = friendsService.settings().showFamilies();

        List<FakePlayer> friends = friendsService.findFriends(player, true).orElse(null);

        if(friends != null && friends.size() != 0) {
            final Component[] playersList = {text("")};

            friends.forEach(friend -> {
                playersList[0] = playersList[0].appendNewline();

                playersList[0] = playersList[0].append(text("[x]", RED).hoverEvent(HoverEvent.showText(text("Unfriend "+friend.username()))).clickEvent(ClickEvent.runCommand("/unfriend " + friend.username())));
                playersList[0] = playersList[0].append(space());


                if(isFriendMessagingEnabled) {
                    playersList[0] = playersList[0].append(text("[!]", YELLOW).hoverEvent(HoverEvent.showText(text("Message " + friend.username()))).clickEvent(ClickEvent.suggestCommand("/fm " + friend.username() + " ")));
                    playersList[0] = playersList[0].append(space());
                }


                if(finalIsPartyEnabled) {
                    playersList[0] = playersList[0].append(text("[p]", BLUE).hoverEvent(HoverEvent.showText(text("Invite " + friend.username() + " to your party"))).clickEvent(ClickEvent.runCommand("/party invite " + friend.username() + " ")));
                    playersList[0] = playersList[0].append(space());
                }


                Player resolvedFriend = friend.resolve().orElse(null);
                if(resolvedFriend == null) {
                    playersList[0] = playersList[0].append(text(friend.username(), GRAY).hoverEvent(HoverEvent.showText(text("Offline", GRAY))));
                    return;
                }
                if(resolvedFriend.getCurrentServer().orElse(null) == null) {
                    playersList[0] = playersList[0].append(text(friend.username(), GRAY).hoverEvent(HoverEvent.showText(text("Offline", GRAY))));
                    return;
                }

                PlayerServer playerServer = api.services().serverService().search(resolvedFriend.getCurrentServer().get().getServerInfo());
                if(canSeeFriendFamilies)
                    playersList[0] = playersList[0].append(text(friend.username(), WHITE).hoverEvent(HoverEvent.showText(text("Currently Playing: ", GRAY).append(text(playerServer.family().name(), AQUA)))));
                else
                    playersList[0] = playersList[0].append(text(friend.username(), WHITE).hoverEvent(HoverEvent.showText(text("Online", WHITE))));
            });

            return join(
                    Lang.newlines(),
                    text("--------------", GRAY)
                            .append(text(" Friends ("+friends.size()+"/"+maxFriends+") ", WHITE))
                            .append(text("[+]", GREEN).hoverEvent(HoverEvent.showText(text("Add Friend"))).clickEvent(ClickEvent.suggestCommand("/friends add <username>")))
                            .append(text(" --------------", GRAY)),
                    playersList[0],
                    space(),
                    text("---------------------------------------------", GRAY)
            );
        }

        return join(
                Lang.newlines(),
                text("Click here to send a friend request.", YELLOW, TextDecoration.UNDERLINED).clickEvent(ClickEvent.suggestCommand("/friends add <username>"))
        );
    };

    ParameterizedMessage1<Player> FRIEND_REQUEST = (sender) -> join(
            Lang.newlines(),
            text("Hey! "+ sender.getUsername() +" wants to be your friend!", NamedTextColor.GRAY),
            join(
                    JoinConfiguration.separator(space()),
                    text("[Accept]", GREEN).hoverEvent(HoverEvent.showText(text("Accept friend request"))).clickEvent(ClickEvent.runCommand("/friends requests "+sender.getUsername()+" accept")),
                    text("[Ignore]", RED).hoverEvent(HoverEvent.showText(text("Ignore friend request"))).clickEvent(ClickEvent.runCommand("/friends requests "+sender.getUsername()+" ignore"))
            )
    );
    ParameterizedMessage1<Player> FRIEND_JOIN = (player) -> {
        FriendsService friendsService = VelocityAPI.get().services().friendsService().orElseThrow();

        if(friendsService.settings().allowMessaging())
            return join(
                    JoinConfiguration.separator(space()),
                    text("Your friend", NamedTextColor.GRAY),
                    text(player.getUsername(), AQUA, TextDecoration.UNDERLINED).hoverEvent(HoverEvent.showText(text("Send a message to "+player.getUsername()))).clickEvent(ClickEvent.suggestCommand("/fm "+player.getUsername()+" ")),
                    text("just logged in.", NamedTextColor.GRAY)
            );
        else
            return text("Your friend "+ player.getUsername() +" just logged in!", NamedTextColor.GRAY);
    };
    ParameterizedMessage1<Player> FRIEND_LEAVE = (player) -> text("Your friend "+ player.getUsername() +" just logged out!", NamedTextColor.GRAY);;
    Message FRIEND_REQUEST_USAGE = () -> text("Usage: /friend requests <username> <accept / ignore>",RED);
    Message UNFRIEND_USAGE = () -> text("Usage: /unfriend <username>",RED);
    Message FM_USAGE = () -> text("Usage: /fm <username> <message>",RED);

    ParameterizedMessage1<ServerInfo> PING = serverInfo -> text(
             LoggerConfig.getConfig().getConsoleIcons_ping() + " " +
                    "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")"
    );

    ParameterizedMessage2<ServerInfo, String> REGISTRATION_REQUEST = (serverInfo, familyName) -> text(
            "["+serverInfo.getName()+"]" +
                    "("+serverInfo.getAddress().getHostName()+":"+serverInfo.getAddress().getPort()+")" +
                    " "+ LoggerConfig.getConfig().getConsoleIcons_attemptingRegistration() +" "+familyName
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
                    " "+ LoggerConfig.getConfig().getConsoleIcons_attemptingUnregistration() +" "+familyName
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

    ParameterizedMessage1<BaseServerFamily> FAMILY_BALANCING = family -> text(
            family.name() + " " + LoggerConfig.getConfig().getConsoleIcons_familyBalancing()
    );
}
