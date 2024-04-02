package group.aelysium.rustyconnector.plugin.velocity.lib.lang;

import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.lang.ASCIIAlphabet;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.core.lib.lang.LanguageResolver;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.ranked_family.RankedFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.Matchmaker;
import group.aelysium.rustyconnector.plugin.velocity.lib.matchmaking.storage.WinLossPlayerRank;
import group.aelysium.rustyconnector.toolkit.velocity.family.IFamily;
import group.aelysium.rustyconnector.toolkit.velocity.family.scalar_family.IRootFamily;
import group.aelysium.rustyconnector.toolkit.velocity.friends.IFriendRequest;
import group.aelysium.rustyconnector.toolkit.velocity.parties.IParty;
import group.aelysium.rustyconnector.toolkit.velocity.player.IPlayer;
import group.aelysium.rustyconnector.toolkit.velocity.server.IMCLoader;
import group.aelysium.rustyconnector.toolkit.velocity.util.LiquidTimestamp;
import group.aelysium.rustyconnector.toolkit.velocity.util.AddressUtil;
import group.aelysium.rustyconnector.plugin.velocity.central.Tinder;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.RootFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.scalar_family.ScalarFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.family.static_family.StaticFamily;
import group.aelysium.rustyconnector.plugin.velocity.lib.friends.FriendsService;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.MCLoader;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class ProxyLang extends Lang {
    public static LanguageResolver resolver() {
        return Tinder.get().lang().resolver();
    }

    public final static String REASON = resolver().getRaw("core.single_word.reason");
    public final static String STATUS = resolver().getRaw("core.single_word.status");
    public final static String ID = resolver().getRaw("core.single_word.id");
    public final static String TIMESTAMP = resolver().getRaw("core.single_word.timestamp");
    public final static String CONTENTS = resolver().getRaw("core.single_word.contents");
    public final static String PAGES = resolver().getRaw("core.single_word.pages");
    public final static String USAGE = resolver().getRaw("core.single_word.usage");
    public final static String LEADER = resolver().getRaw("core.single_word.leader");
    public final static String PARTY = resolver().getRaw("core.single_word.party");
    public final static String LEAVE = resolver().getRaw("core.single_word.leave");
    public final static String DISBAND = resolver().getRaw("core.single_word.disband");
    public final static String ACCEPT = resolver().getRaw("core.single_word.accept");
    public final static String DENY = resolver().getRaw("core.single_word.deny");
    public final static String IGNORE = resolver().getRaw("core.single_word.ignore");
    public final static String DATE = resolver().getRaw("core.single_word.date");

    public final static Component BORDER = text("█████████████████████████████████████████████████████████████████████████████████████████████████", DARK_GRAY);

    public final static Component SPACING = text("");

    public final static Component UNKNOWN_COMMAND = text(resolver().getRaw("core.unknown_command"));
    public final static Component NO_PERMISSION = text(resolver().getRaw("core.no_permission"));
    public final static Component INTERNAL_ERROR = resolver().get("core.internal_error");

    public final static Component WORDMARK_USAGE = ASCIIAlphabet.generate("usage");

    public final static Component WORDMARK_MESSAGE = ASCIIAlphabet.generate("message");
    public final static Component SERVER_ALREADY_CONNECTED = resolver().get("proxy.server.already_connected");
    public final static ParameterizedMessage1<String> NO_PLAYER = (username) ->
            resolver().get("core.no_player", LanguageResolver.tagHandler("username", username));

    public final static ParameterizedMessage1<String> WORDMARK_RUSTY_CONNECTOR = (version) -> {// font: ANSI Shadow
        Component versionComponent = empty();

        if(version != null && !version.equals(""))
            versionComponent = versionComponent.append(text("Version "+version, GREEN));

        return join(
                newlines(),
                BORDER,
                SPACING,
                text(" /███████                        /██", AQUA),
                text("| ██__  ██                      | ██", AQUA),
                text("| ██  \\ ██ /██   /██  /███████ /██████   /██   /██", AQUA),
                text("| ███████/| ██  | ██ /██_____/|_  ██_/  | ██  | ██", AQUA),
                text("| ██__  ██| ██  | ██|  ██████   | ██    | ██  | ██", AQUA),
                text("| ██  \\ ██| ██  | ██ \\____  ██  | ██ /██| ██  | ██", AQUA),
                text("| ██  | ██|  ██████/ /███████/  |  ████/|  ███████", AQUA),
                text("|__/  |__/ \\______/ |_______/    \\___/   \\____  ██", AQUA),
                text("                                         /██  | ██  ", AQUA).append(versionComponent),
                text("                                        |  ██████/", AQUA),
                text("  /██████                                \\______/             /██", AQUA),
                text(" /██__  ██                                                    | ██", AQUA),
                text("| ██  \\__/  /██████  /███████  /███████   /██████   /███████ /██████    /██████   /██████", AQUA),
                text("| ██       /██__  ██| ██__  ██| ██__  ██ /██__  ██ /██_____/|_  ██_/   /██__  ██ /██__  ██", AQUA),
                text("| ██      | ██  \\ ██| ██  \\ ██| ██  \\ ██| ████████| ██        | ██    | ██  \\ ██| ██  \\__/", AQUA),
                text("| ██    ██| ██  | ██| ██  | ██| ██  | ██| ██_____/| ██        | ██ /██| ██  | ██| ██", AQUA),
                text("|  ██████/|  ██████/| ██  | ██| ██  | ██|  ███████|  ███████  |  ████/|  ██████/| ██", AQUA),
                text("\\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/", AQUA),
                SPACING,
                BORDER,
                SPACING,
                resolver().get("core.boot_wordmark.developed_by").append(text(" Aelysium | Juice")),
                resolver().get("core.boot_wordmark.usage").color(YELLOW),
                SPACING,
                BORDER
        );
    };

    public final static ParameterizedMessage1<Component> BOXED_MESSAGE = (message) -> join(
            newlines(),
            SPACING,
            BORDER,
            SPACING,
            message,
            SPACING,
            BORDER,
            SPACING
    );
    public final static ParameterizedMessage2<String, NamedTextColor> BOXED_MESSAGE_COLORED = (message, color) -> join(
            newlines(),
            SPACING,
            BORDER.color(color),
            SPACING,
            text(message).color(color),
            SPACING,
            BORDER.color(color),
            SPACING
    );
    public final static ParameterizedMessage2<Component, NamedTextColor> BOXED_COMPONENT_COLORED = (message, color) -> join(
            newlines(),
            SPACING,
            BORDER.color(color),
            SPACING,
            message.color(color),
            SPACING,
            BORDER.color(color),
            SPACING
    );

    public final static ParameterizedMessage1<CacheableMessage> CACHED_MESSAGE = (message) -> join(
            newlines(),
            BORDER,
            text(STATUS+": " + message.getSentence().name(), message.getSentence().color()),
            text(ID+": ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
            text(TIMESTAMP+": ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
            text( CONTENTS+": ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
            BORDER
    );

    public final static ParameterizedMessage3<List<CacheableMessage>,Integer, Integer> RC_MESSAGE_PAGE = (messages, pageNumber, maxPages) -> {
        Component output = text("");
        for (CacheableMessage message : messages) {
            if(!(message.getSentenceReason() == null))
                output = output.append(join(
                        newlines(),
                        BORDER,
                        SPACING,
                        text(STATUS+": " + message.getSentence().name(), message.getSentence().color()),
                        text(REASON+": " + message.getSentenceReason(), message.getSentence().color()),
                        SPACING,
                        text(ID+": ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text(TIMESTAMP+": ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text(CONTENTS+": ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                        SPACING
                ));
            else
                output = output.append(join(
                        newlines(),
                        BORDER,
                        SPACING,
                        text(STATUS+": " + message.getSentence().name(), message.getSentence().color()),
                        SPACING,
                        text(ID+": ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text(TIMESTAMP+": ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text(CONTENTS+": ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                        SPACING
                ));
        }

        Component pageNumbers = text("[ ",DARK_GRAY);
        for (int i = 1; i <= maxPages; i++) {
            if(i == pageNumber)
                pageNumbers = pageNumbers.append(text(i+" ",GOLD));
            else
                pageNumbers = pageNumbers.append(text(i+" ",GRAY));
        }
        pageNumbers = pageNumbers.append(text("]",DARK_GRAY));

        return output.append(
                join(
                        newlines(),
                        SPACING,
                        BORDER,
                        SPACING,
                        text(PAGES+":"),
                        pageNumbers,
                        SPACING,
                        BORDER
                )
        );
    };

    public final static Component WORDMARK_REGISTERED_FAMILIES =
            join(
                    newlines(),
                    ASCIIAlphabet.generate("registered"),
                    SPACING,
                    ASCIIAlphabet.generate("families")
            );

    public final static Component RC_ROOT_USAGE = join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.color(AQUA),
            SPACING,
            resolver().getArray("proxy.root.usage.description"),
            SPACING,
            BORDER,
            SPACING,
            text("/rc family", AQUA),
            resolver().get("proxy.root.usage.command_description.family"),
            SPACING,
            text("/rc message", AQUA),
            resolver().get("proxy.root.usage.command_description.message"),
            SPACING,
            text("/rc reload", GOLD),
            resolver().get("proxy.root.usage.command_description.reload"),
            SPACING,
            text("/rc send", AQUA),
            resolver().get("proxy.root.usage.command_description.send"),
            SPACING,
            BORDER
    );

    public final static Component RC_MESSAGE_ROOT_USAGE = join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc message get <Message ID>", AQUA),
            resolver().get("proxy.message.usage.get"),
            SPACING,
            text("/rc message list <page number>", AQUA),
            resolver().get("proxy.message.usage.list"),
            SPACING,
            BORDER
    );

    public final static Component RC_SEND_USAGE = join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc send <username> <family id>", GOLD),
            resolver().get("proxy.send.usage.family"),
            SPACING,
            text("/rc send server <username> <server id>", GOLD),
            resolver().getArray("proxy.send.usage.server"),
            SPACING,
            BORDER
    );

    public final static Component RC_MESSAGE_GET_USAGE = join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text("/rc message get <Message ID>",AQUA),
            resolver().get("proxy.message.usage.get"),
            SPACING,
            BORDER
    );

    public final static ParameterizedMessage1<String> RC_SEND_NO_PLAYER = username ->
            resolver().get("proxy.send.no_player", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> RC_SEND_NO_FAMILY = familyName ->
            resolver().get("proxy.send.no_family", LanguageResolver.tagHandler("family_name", familyName));
    public final static ParameterizedMessage1<String> RC_SEND_NO_SERVER = serverName ->
            resolver().get("proxy.send.no_server", LanguageResolver.tagHandler("server_name", serverName));
    public final static Message RC_SEND_SAME_FAMILY = () -> resolver().get("proxy.send.same_family");

    public final static ParameterizedMessage1<CacheableMessage> RC_MESSAGE_GET_MESSAGE = (message) -> join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.color(AQUA),
            SPACING,
            BORDER,
            SPACING,
            text(STATUS+": " + message.getSentence().name(), message.getSentence().color()),
            text(REASON+": " + message.getSentenceReason(), message.getSentence().color()),
            SPACING,
            text(ID+": ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
            text(TIMESTAMP+": ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
            text(CONTENTS+": ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
            SPACING
    );

    public final static ParameterizedMessage1<String> RC_MESSAGE_ERROR = error -> join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.color(RED),
            SPACING,
            BORDER,
            SPACING,
            text(error,GRAY),
            SPACING,
            BORDER
    );

    public final static Message RC_FAMILY = () -> {
        Tinder api = Tinder.get();
        Component families = text("");
        for (IFamily family : api.services().family().dump()) {
            if(family instanceof RootFamily)
                families = families.append(text("["+family.id()+"*] ").color(BLUE));
            if(family instanceof ScalarFamily)
                families = families.append(text("["+family.id()+"] ").color(BLUE));
            if(family instanceof StaticFamily)
                families = families.append(text("["+family.id()+"] ").color(DARK_GREEN));
            if(family instanceof RankedFamily)
                families = families.append(text("["+family.id()+"] ").color(YELLOW));
        }

        return join(
                newlines(),
                BORDER,
                SPACING,
                WORDMARK_REGISTERED_FAMILIES.color(AQUA),
                SPACING,
                BORDER,
                SPACING,
                resolver().getArray("proxy.family.description"),
                families,
                SPACING,
                BORDER,
                SPACING,
                text("/rc family <family id>",DARK_AQUA),
                resolver().get("proxy.family.details_usage"),
                SPACING,
                BORDER
        );
    };

    public final static ParameterizedMessage2<ScalarFamily, Boolean> RC_SCALAR_FAMILY_INFO = (family, locked) -> {
        Component servers = text("");
        int i = 0;

        if(family.registeredServers() == null) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.registeredServers().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.loadBalancer().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_unlocked_servers");
        else {
            List<IMCLoader> serverList;
            if(locked)
                serverList = family.loadBalancer().lockedServers();
            else
                serverList = family.loadBalancer().openServers();

            for (IMCLoader server : serverList) {
                Component serverEntry = resolver().get(
                        "proxy.family.generic.servers.details",
                        LanguageResolver.tagHandler("index_number", i + 1),
                        LanguageResolver.tagHandler("server_name", server.uuidOrDisplayName()),
                        LanguageResolver.tagHandler("server_address", AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress())),
                        LanguageResolver.tagHandler("player_count", server.playerCount()),
                        LanguageResolver.tagHandler("player_soft_cap", server.softPlayerCap()),
                        LanguageResolver.tagHandler("player_hard_cap", server.hardPlayerCap()),
                        LanguageResolver.tagHandler("server_weight", server.weight())
                );

                if(family.loadBalancer().index() == i && !locked)
                    serverEntry = serverEntry.color(GREEN);
                else
                    serverEntry = serverEntry.color(GRAY);

                servers = servers.append(serverEntry).append(newline());

                i++;
            }
        }

        IRootFamily rootFamily = Tinder.get().services().family().rootFamily();
        String parentFamilyName = rootFamily.id();
        try {
            parentFamilyName = Objects.requireNonNull(family.parent()).id();
        } catch (Exception ignore) {}
        if(family.equals(rootFamily)) parentFamilyName = "none";

        String persistence = "Disabled";
        if(family.loadBalancer().persistent())
            persistence = family.loadBalancer().attempts() + " Attempts";

        return join(
                newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.id(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                resolver().getArray(
                        "proxy.family.scalar.panel.info",
                        LanguageResolver.tagHandler("display_name", family.displayName()),
                        LanguageResolver.tagHandler("parent_family_name", parentFamilyName),
                        LanguageResolver.tagHandler("players_count", family.playerCount()),

                        LanguageResolver.tagHandler("servers_count", family.loadBalancer().size()),
                        LanguageResolver.tagHandler("servers_open", family.loadBalancer().size(false)),
                        LanguageResolver.tagHandler("servers_locked", family.loadBalancer().size(true)),

                        LanguageResolver.tagHandler("load_balancing_algorithm", family.loadBalancer()),
                        LanguageResolver.tagHandler("load_balancing_weighted", family.loadBalancer().weighted()),
                        LanguageResolver.tagHandler("load_balancing_persistence", persistence)
                ),
                SPACING,
                BORDER,
                SPACING,
                resolver().get("proxy.family.generic.servers.open_servers"),
                SPACING,
                text("/rc family <family id> sort", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.sort"),
                SPACING,
                text("/rc family <family id> resetIndex", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.reset_index"),
                SPACING,
                text("/rc family <family id> locked", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.locked"),
                SPACING,
                servers,
                SPACING,
                BORDER
        );
    };

    public final static ParameterizedMessage2<StaticFamily, Boolean> RC_STATIC_FAMILY_INFO = (family, locked) -> {
        Component servers = text("");
        int i = 0;

        if(family.registeredServers() == null) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.registeredServers().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.loadBalancer().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_unlocked_servers");
        else {
            List<IMCLoader> serverList;
            if(locked)
                serverList = family.loadBalancer().lockedServers();
            else
                serverList = family.loadBalancer().openServers();

            for (IMCLoader server : serverList) {
                Component serverEntry = resolver().get(
                        "proxy.family.generic.servers.details",
                        LanguageResolver.tagHandler("index_number", i + 1),
                        LanguageResolver.tagHandler("server_name", server.uuidOrDisplayName()),
                        LanguageResolver.tagHandler("server_address", AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress())),
                        LanguageResolver.tagHandler("player_count", server.playerCount()),
                        LanguageResolver.tagHandler("player_soft_cap", server.softPlayerCap()),
                        LanguageResolver.tagHandler("player_hard_cap", server.hardPlayerCap()),
                        LanguageResolver.tagHandler("server_weight", server.weight())
                );

                if(family.loadBalancer().index() == i && !locked)
                    serverEntry = serverEntry.color(GREEN);
                else
                    serverEntry = serverEntry.color(GRAY);

                servers = servers.append(serverEntry).append(newline());

                i++;
            }
        }

        // Compile residence expiration
        LiquidTimestamp expiration = family.homeServerExpiration();
        String homeServerExpiration = "NEVER";
        if(expiration != null) homeServerExpiration = expiration.toString();

        // Compile Persistence
        String persistence = "Disabled";
        if(family.loadBalancer().persistent())
            persistence = family.loadBalancer().attempts() + " Attempts";

        return join(
                newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.id(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                resolver().getArray(
                        "proxy.family.static.panel.info",
                        LanguageResolver.tagHandler("display_name", family.displayName()),
                        LanguageResolver.tagHandler("parent_family_name", family.parent().id()),
                        LanguageResolver.tagHandler("player_count", family.playerCount()),

                        LanguageResolver.tagHandler("residence_expiration", homeServerExpiration),
                        LanguageResolver.tagHandler("servers_count", family.loadBalancer().size()),
                        LanguageResolver.tagHandler("servers_open", family.loadBalancer().size(false)),
                        LanguageResolver.tagHandler("servers_locked", family.loadBalancer().size(true)),

                        LanguageResolver.tagHandler("load_balancing_algorithm", family.loadBalancer()),
                        LanguageResolver.tagHandler("load_balancing_weighted", family.loadBalancer().weighted()),
                        LanguageResolver.tagHandler("load_balancing_persistence", persistence)
                ),
                SPACING,
                BORDER,
                SPACING,
                resolver().get("proxy.family.generic.servers.open_servers"),
                SPACING,
                text("/rc family <family id> sort", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.sort"),
                SPACING,
                text("/rc family <family id> resetIndex", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.reset_index"),
                SPACING,
                text("/rc family <family id> locked", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.locked"),
                SPACING,
                servers,
                SPACING,
                BORDER
        );
    };

    public final static Message RANKED_FAMILY_PARTY_DENIAL = () -> resolver().get("proxy.family.ranked.in_party");
    public final static ParameterizedMessage2<RankedFamily, Boolean> RC_RANKED_FAMILY_INFO = (family, locked) -> {
        Component servers = text("");
        int i = 0;

        if(family.registeredServers() == null) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.registeredServers().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_registered_servers");
        else if(family.loadBalancer().size() == 0) servers = resolver().get("proxy.family.generic.servers.no_unlocked_servers");
        else {
            List<IMCLoader> serverList;
            if(locked)
                serverList = family.loadBalancer().lockedServers();
            else
                serverList = family.loadBalancer().openServers();

            for (IMCLoader server : serverList) {
                Component serverEntry = resolver().get(
                        "proxy.family.generic.servers.details",
                        LanguageResolver.tagHandler("index_number", i + 1),
                        LanguageResolver.tagHandler("server_name", server.uuidOrDisplayName()),
                        LanguageResolver.tagHandler("server_address", AddressUtil.addressToString(server.registeredServer().getServerInfo().getAddress())),
                        LanguageResolver.tagHandler("player_count", server.playerCount()),
                        LanguageResolver.tagHandler("player_soft_cap", server.softPlayerCap()),
                        LanguageResolver.tagHandler("player_hard_cap", server.hardPlayerCap()),
                        LanguageResolver.tagHandler("server_weight", server.weight())
                );

                if(family.loadBalancer().index() == i && !locked)
                    serverEntry = serverEntry.color(GREEN);
                else
                    serverEntry = serverEntry.color(GRAY);

                servers = servers.append(serverEntry).append(newline());

                i++;
            }
        }

        String algorithm = "RANDOMIZE";
        Matchmaker matchmaker = family.matchmaker();
        if(matchmaker.settings().ranking().schema().equals(WinLossPlayerRank.class)) algorithm = "WIN_LOSS";
        if(matchmaker.settings().ranking().schema().equals(WinLossPlayerRank.class)) algorithm = "WIN_RATE";

        return join(
                newlines(),
                BORDER,
                SPACING,
                ASCIIAlphabet.generate(family.id(), AQUA),
                SPACING,
                BORDER,
                SPACING,
                resolver().getArray(
                        "proxy.family.ranked.panel.info",
                        LanguageResolver.tagHandler("display_name", family.displayName()),
                        LanguageResolver.tagHandler("parent_family_name", family.parent().id()),

                        LanguageResolver.tagHandler("servers_count", family.loadBalancer().size()),
                        LanguageResolver.tagHandler("servers_open", family.loadBalancer().size(false)),
                        LanguageResolver.tagHandler("servers_locked", family.loadBalancer().size(true)),

                        LanguageResolver.tagHandler("session_count", matchmaker.sessionCount()),
                        LanguageResolver.tagHandler("active_sessions", matchmaker.activeSessionCount()),
                        LanguageResolver.tagHandler("waiting_sessions", matchmaker.queuedSessionCount()),

                        LanguageResolver.tagHandler("player_count", matchmaker.playerCount()),
                        LanguageResolver.tagHandler("active_players", matchmaker.activePlayerCount()),
                        LanguageResolver.tagHandler("waiting_players", matchmaker.queuedPlayerCount()),

                        LanguageResolver.tagHandler("matchmaking_algorithm", algorithm)
                ),
                SPACING,
                BORDER,
                SPACING,
                resolver().get("proxy.family.generic.servers.open_servers"),
                SPACING,
                text("/rc family <family id> sort", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.sort"),
                SPACING,
                text("/rc family <family id> resetIndex", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.reset_index"),
                SPACING,
                text("/rc family <family id> locked", GOLD),
                resolver().get("proxy.family.generic.command_descriptions.locked"),
                SPACING,
                servers,
                SPACING,
                BORDER
        );
    };

    public final static Component MISSING_HOME_SERVER = resolver().get("proxy.family.static.residence.missing");
    public final static Component BLOCKED_STATIC_FAMILY_JOIN_ATTEMPT = resolver().get("proxy.family.static.residence.blocked_join_attempt");

    public final static Component TPA_USAGE = text(USAGE+": /tpa <<username>, deny, accept>",RED);
    public final static Message TPA_DENY_USAGE = () -> join(
            newlines(),
            text(USAGE+": /tpa deny <username>",RED),
            resolver().get("proxy.tpa.usage.deny")
    );
    public final static Message TPA_ACCEPT_USAGE = () -> join(
            newlines(),
            text(USAGE+": /tpa accept <username>",RED),
            resolver().get("proxy.tpa.usage.accept")
    );

    public final static ParameterizedMessage1<String> TPA_FAILURE = username -> resolver().get("proxy.tpa.sender_failure", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_FAILURE_TARGET = username -> resolver().get("proxy.tpa.target_failure", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_NOT_FRIENDS = username -> resolver().get("proxy.tpa.not_friends", LanguageResolver.tagHandler("username", username));
    public final static Component TPA_FAILURE_SELF_TP = resolver().get("proxy.tpa.self_failure");
    public final static ParameterizedMessage1<String> TPA_FAILURE_NO_USERNAME = username -> resolver().get("proxy.tpa.not_online", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_FAILURE_NO_REQUEST = username -> resolver().get("proxy.tpa.no_requests", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_DUPLICATE = username -> resolver().get("proxy.tpa.pending_request", LanguageResolver.tagHandler("username", username));

    public final static ParameterizedMessage1<IPlayer> TPA_REQUEST_QUERY = (sender) -> join(
            newlines(),
            resolver().get("proxy.tpa.target_query.query", LanguageResolver.tagHandler("username", sender.username())),
            join(
                    JoinConfiguration.separator(space()),
                    text("["+ACCEPT+"]", GREEN).hoverEvent(HoverEvent.showText(resolver().get("proxy.tpa.target_query.accept_tooltip", LanguageResolver.tagHandler("username", sender.username())))).clickEvent(ClickEvent.runCommand("/tpa accept "+sender.username())),
                    text("["+DENY+"]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.tpa.target_query.deny_tooltip", LanguageResolver.tagHandler("username", sender.username())))).clickEvent(ClickEvent.runCommand("/tpa deny "+sender.username()))
            )
    );
    public final static ParameterizedMessage1<String> TPA_REQUEST_SUBMISSION = username -> resolver().get("proxy.tpa.request_confirmation", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_ACCEPTED_SENDER = username -> resolver().getArray("proxy.tpa.sender_accepted", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_ACCEPTED_TARGET = username -> resolver().getArray("proxy.tpa.target_accepted", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_DENIED_SENDER = username -> resolver().getArray("proxy.tpa.sender_deny", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_DENIED_TARGET = username -> resolver().getArray("proxy.tpa.target_deny", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> TPA_REQUEST_EXPIRED = username -> resolver().get("proxy.tpa.expired_request", LanguageResolver.tagHandler("username", username));

    public final static ParameterizedMessage1<String> TPA_REQUEST_BYPASSED = username -> resolver().get("proxy.tpa.bypassed_request", LanguageResolver.tagHandler("username", username));

    public final static Component HUB_CONNECTION_FAILED = resolver().get("proxy.hub.connection_failed");

    public final static ParameterizedMessage2<IParty, IPlayer> PARTY_BOARD = (party, member) -> {
        boolean hasParty = party != null;

        if(hasParty) {
            boolean isLeader = party.leader().equals(member);
            boolean canInvite;
            try {
                boolean onlyLeaderCanInvite = Tinder.get().services().party().orElseThrow().settings().onlyLeaderCanInvite();
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
                                        text("[x]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.leave"))).clickEvent(ClickEvent.runCommand("/party leave")),
                                        text("[^]", GRAY),
                                        text(partyMember.username(), WHITE),
                                        text("["+LEADER+"]", BLUE)
                                )
                        );
                    else
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text("[x]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.kick"))).clickEvent(ClickEvent.runCommand("/party kick " + partyMember.username())),
                                        text("[^]", GREEN).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.promote"))).clickEvent(ClickEvent.runCommand("/party promote " + partyMember.username())),
                                        text(partyMember.username(), WHITE)
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
                                        text(partyMember.username(), WHITE),
                                        text("["+LEADER+"]", BLUE)
                                )
                        );
                    else
                        playersList[0] = playersList[0].append(
                                join(
                                        JoinConfiguration.separator(text(" ")),
                                        text(partyMember.username(), WHITE)
                                )
                        );
                });

            Component header;
            if(canInvite)
                header = text("-------------------", GRAY)
                 .append(text(" "+PARTY+" ", WHITE))
                 .append(text("[+]", GREEN)).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.invite_player"))).clickEvent(ClickEvent.suggestCommand("/party invite <username>"))
                 .append(text(" ------------------", GRAY));
            else
                header = text("------------------", GRAY)
                        .append(text(" "+PARTY+" ", WHITE))
                        .append(text(" ------------------", GRAY));

            if(isLeader)
                return join(
                        newlines(),
                        header,
                        playersList[0],
                        space(),
                        text("----------------", GRAY)
                                .appendSpace()
                                .append(text(DISBAND, RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party disband")))
                                .appendSpace()
                                .appendSpace()
                                .append(text(LEAVE, RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party leave")))
                                .appendSpace()
                                .append(text("----------------", GRAY))
                );
            else
                return join(
                        newlines(),
                        header,
                        playersList[0],
                        space(),
                        text("------------------ ", GRAY).append(text(LEAVE, RED, TextDecoration.UNDERLINED).clickEvent(ClickEvent.runCommand("/party leave"))).append(text(" ------------------", GRAY))
                        );
        }

        return resolver().get("proxy.party.create.button").clickEvent(ClickEvent.runCommand("/party create"));
    };

    public final static ParameterizedMessage1<String> PARTY_INVITE_RECEIVED = (username) -> join(
            newlines(),
            resolver().get("proxy.party.receiver_invite_query.query", LanguageResolver.tagHandler("username", username)),
            join(
                    JoinConfiguration.separator(space()),
                    text("["+ACCEPT+"]", GREEN).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.receiver_invite_query.hover.accept"))).clickEvent(ClickEvent.runCommand("/party invites "+username+" accept")),
                    text("["+IGNORE+"]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.party.receiver_invite_query.hover.ignore"))).clickEvent(ClickEvent.runCommand("/party invites "+username+" ignore"))
            )
    );

    public final static Component PARTY_USAGE_INVITES = text(USAGE+": /party invites <username> <accept / ignore>",RED);

    public final static Component PARTY_USAGE_INVITE = text(USAGE+": /party invite <username>",RED);

    public final static Component PARTY_USAGE_KICK = text(USAGE+": /party kick <username>",RED);

    public final static Component PARTY_USAGE_PROMOTE = text(USAGE+": /party promote <username>",RED);

    public final static Component PARTY_DISBANDED = resolver().get("proxy.party.disbanded");
    public final static Component PARTY_JOINED_SELF = resolver().get("proxy.party.party_joined_self");
    public final static Component PARTY_INVITE_NO_DOUBLE_DIPPING = resolver().get("proxy.party.invite.no_double_dipping");
    public final static Component PARTY_INVITE_EXPIRED = resolver().get("proxy.party.invite.expired");
    public final static ParameterizedMessage1<String> PARTY_INVITE_TARGET_NOT_ONLINE = (username) ->
            resolver().get("proxy.party.invite.target_not_online", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> PARTY_JOINED = (username) ->
            resolver().get("proxy.party.party_joined", LanguageResolver.tagHandler("username", username));
    public final static Component PARTY_FOLLOWING_KICKED_GENERIC = resolver().get("proxy.party.following_kicked.generic");
    public final static Component PARTY_FOLLOWING_KICKED_SERVER_FULL = resolver().get("proxy.party.following_kicked.server_full");
    public final static Component PARTY_FOLLOWING_FAILED_GENERIC = resolver().get("proxy.party.following_failed.generic");
    public final static Component PARTY_FOLLOWING_FAILED_SERVER_FULL = resolver().get("proxy.party.following_failed.server_full");
    public final static ParameterizedMessage1<String> PARTY_INVITE_SENT = (username) -> resolver().get(
            "proxy.party.invite.sent",
            LanguageResolver.tagHandler("username", username)
    );
    public final static Component PARTY_INVITE_NOT_ONLINE = resolver().get("proxy.party.invite.not_online");
    public final static Component PARTY_INVITE_FRIENDS_ONLY = resolver().get("proxy.party.invite.friends_only");
    public final static Component PARTY_INVITE_SELF_INVITE = resolver().get("proxy.party.invite.self_invite");
    public final static ParameterizedMessage1<String> PARTY_INVITE_ALREADY_A_MEMBER = (username) -> resolver().get(
            "proxy.party.invite.already_a_member",
            LanguageResolver.tagHandler("username", username)
    );
    public final static Component PARTY_CREATED = resolver().get("proxy.party.created");
    public final static Component PARTY_KICKED = resolver().get("proxy.party.kicked");
    public final static Component PARTY_PROMOTED = resolver().get("proxy.party.promoted");
    public final static Component NO_PARTY = resolver().get("proxy.party.no_party");
    public final static ParameterizedMessage1<String> PARTY_NO_MEMBER = (username) -> resolver().get(
            "proxy.party.no_member",
            LanguageResolver.tagHandler("username", username)
    );
    public final static Component PARTY_LEFT_SELF = resolver().get("proxy.party.left_self");
    public final static Component PARTY_CREATE_ALREADY_IN_PARTY = resolver().get("proxy.party.create.already_in_party");
    public final static Component PARTY_CREATE_NO_SERVER = resolver().get("proxy.party.create.no_server");
    public final static ParameterizedMessage1<com.velocitypowered.api.proxy.Player> PARTY_STATUS_PROMOTED = (player) -> resolver().get(
            "proxy.party.status_promoted",
            LanguageResolver.tagHandler("username", player.getUsername())
    );
    public final static Component PARTY_DEMOTED = resolver().get("proxy.party.demoted");
    public final static Component PARTY_ALREADY_LEADER = resolver().get("proxy.party.already_leader");
    public final static Component PARTY_SELF_KICK = resolver().get("proxy.party.self_kick");
    public final static Component PARTY_ONLY_LEADER_CAN_DISBAND = resolver().get("proxy.party.only_leader_can.disband");
    public final static Component PARTY_ONLY_LEADER_CAN_SWITCH = resolver().get("proxy.party.only_leader_can.switch");
    public final static Component PARTY_ONLY_LEADER_CAN_KICK = resolver().get("proxy.party.only_leader_can.kick");
    public final static Component PARTY_ONLY_LEADER_CAN_PROMOTE = resolver().get("proxy.party.only_leader_can.promote");
    public final static Component PARTY_ONLY_LEADER_CAN_INVITE = resolver().get("proxy.party.only_leader_can.invite");
    public final static ParameterizedMessage1<String> PARTY_NO_INVITE = (sender) -> resolver().get("proxy.party.invite.no_invite", LanguageResolver.tagHandler("username", sender));
    public final static ParameterizedMessage1<String> PARTY_IGNORE_INVITE = (sender) -> resolver().get("proxy.party.invite.ignore", LanguageResolver.tagHandler("username", sender));


    public final static String PARTY_INJECTED_ONLY_LEADER_CAN_INVITE = resolver().getRaw("proxy.party.injected_error.only_leader_can_invite");
    public final static String PARTY_INJECTED_FRIENDS_RESTRICTION_CONFLICT = resolver().getRaw("proxy.party.injected_error.friends_restriction_conflict");
    public final static String PARTY_INJECTED_FRIENDS_RESTRICTION = resolver().getRaw("proxy.party.injected_error.friends_restriction");
    public final static String PARTY_INJECTED_ACKNOWLEDGED = resolver().getRaw("proxy.party.injected_error.acknowledged");
    public final static String PARTY_INJECTED_EXPIRED_INVITE = resolver().getRaw("proxy.party.injected_error.expired_invite");
    public final static String PARTY_INJECTED_NO_SENDER = resolver().getRaw("proxy.party.injected_error.no_sender");
    public final static String PARTY_INJECTED_NO_TARGET = resolver().getRaw("proxy.party.injected_error.no_target");
    public final static String PARTY_INJECTED_INVALID_LEADER_INVITE = resolver().getRaw("proxy.party.injected_error.invalid_leader_invite");
    public final static String PARTY_INJECTED_INVALID_MEMBER_INVITE = resolver().getRaw("proxy.party.injected_error.invalid_member_invite");

    public final static ParameterizedMessage1<IPlayer> FRIENDS_BOARD = (player) -> {
        Tinder api = Tinder.get();
        FriendsService friendsService = api.services().friends().orElseThrow();
        int maxFriends = friendsService.settings().maxFriends();
        player.sendMessage(resolver().get("proxy.friends.panel.pending"));

        boolean isPartyEnabled = false;
        try {
            api.services().party().orElseThrow();
            isPartyEnabled = true;
        } catch (Exception ignore) {}
        boolean finalIsPartyEnabled = isPartyEnabled;

        boolean isFriendMessagingEnabled = friendsService.settings().allowMessaging();
        boolean canSeeFriendFamilies = friendsService.settings().showFamilies();

        List<IPlayer> friends = friendsService.findFriends(player).orElse(List.of());

        if(friends.size() != 0) {
            final Component[] playersList = {text("")};

            friends.forEach(friend -> {
                playersList[0] = playersList[0].appendNewline();

                playersList[0] = playersList[0].append(text("[x]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.unfriend", LanguageResolver.tagHandler("username",friend.username())))).clickEvent(ClickEvent.runCommand("/unfriend " + friend.username())));

                playersList[0] = playersList[0].append(space());


                if(isFriendMessagingEnabled) {
                    playersList[0] = playersList[0].append(text("[m]", YELLOW).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.message", LanguageResolver.tagHandler("username",friend.username())))).clickEvent(ClickEvent.suggestCommand("/fm " + friend.username() + " ")));
                    playersList[0] = playersList[0].append(space());
                }

                if(finalIsPartyEnabled) {
                    playersList[0] = playersList[0].append(text("[p]", BLUE).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.invite_party", LanguageResolver.tagHandler("username",friend.username())))).clickEvent(ClickEvent.runCommand("/party invite " + friend.username() + " ")));
                    playersList[0] = playersList[0].append(space());
                }


                com.velocitypowered.api.proxy.Player resolvedFriend = friend.resolve().orElse(null);
                if(resolvedFriend == null) {
                    playersList[0] = playersList[0].append(text(friend.username(), GRAY).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.offline"))));
                    return;
                }
                if(resolvedFriend.getCurrentServer().orElse(null) == null) {
                    playersList[0] = playersList[0].append(text(friend.username(), GRAY).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.offline"))));
                    return;
                }

                MCLoader mcLoader = new MCLoader.Reference(UUID.fromString(resolvedFriend.getCurrentServer().orElseThrow().getServerInfo().getName())).get();
                if(canSeeFriendFamilies)
                    playersList[0] = playersList[0].append(text(friend.username(), WHITE).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.currently_playing", LanguageResolver.tagHandler("family_name", mcLoader.family().displayName())))));
                else
                    playersList[0] = playersList[0].append(text(friend.username(), WHITE).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.online"))));
            });

            return join(
                    newlines(),
                    text("--------------", GRAY)
                            .append(resolver().get("proxy.friends.panel.header.main", LanguageResolver.tagHandler("friend_count", friends.size()), LanguageResolver.tagHandler("max_friends", maxFriends)))
                            .append(text("[+]", GREEN).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.panel.add_friend"))).clickEvent(ClickEvent.suggestCommand("/friends add <username>")))
                            .append(text(" --------------", GRAY)),
                    playersList[0],
                    space(),
                    text("---------------------------------------------", GRAY)
            );
        }

        return join(
                newlines(),
                resolver().get("proxy.friends.panel.send_friend_request").clickEvent(ClickEvent.suggestCommand("/friends add <username>"))
        );
    };

    public final static ParameterizedMessage1<IPlayer> FRIEND_REQUEST = (sender) -> join(
            newlines(),
            resolver().get("proxy.friends.friend_request_query.query", LanguageResolver.tagHandler("username", sender.username())),
            join(
                    JoinConfiguration.separator(space()),
                    text("["+ACCEPT+"]", GREEN).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.friend_request_query.hover.accept"))).clickEvent(ClickEvent.runCommand("/friends requests "+sender.username()+" accept")),
                    text("["+IGNORE+"]", RED).hoverEvent(HoverEvent.showText(resolver().get("proxy.friends.friend_request_query.hover.ignore"))).clickEvent(ClickEvent.runCommand("/friends requests "+sender.username()+" ignore"))
            )
    );
    public final static ParameterizedMessage1<IPlayer> FRIEND_JOIN = (player) -> {
        FriendsService friendsService = Tinder.get().services().friends().orElseThrow();

        if(friendsService.settings().allowMessaging())
            return resolver().get("proxy.friends.friend_joined.resolved", LanguageResolver.tagHandler("username", player.username()));
        else
            return resolver().get("proxy.friends.friend_joined.regular", LanguageResolver.tagHandler("username", player.username()));
    };
    public final static ParameterizedMessage1<IPlayer> FRIEND_LEAVE = (player) ->
            resolver().get("proxy.friends.friend_leaves", LanguageResolver.tagHandler("username", player.username()));
    public final static Message FRIEND_REQUEST_USAGE = () -> text(USAGE+": /friend requests <username> <accept / ignore>",RED);
    public final static ParameterizedMessage1<String> BECOME_FRIENDS = (username) ->
            resolver().get("proxy.friends.become_friends", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> FRIEND_REQUEST_SENT = (username) ->
            resolver().get("proxy.friends.request.sent", LanguageResolver.tagHandler("username", username));
    public final static Component FRIEND_REQUEST_EXPIRED = resolver().get("proxy.friends.request.expired");
    public final static ParameterizedMessage1<String> FRIEND_REQUEST_IGNORE = (username) ->
            resolver().get("proxy.friends.request.ignore", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> FRIEND_REQUEST_TARGET_NOT_ONLINE = (username) ->
            resolver().get("proxy.friends.request.target_not_online", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> UNFRIEND_NOT_FRIENDS = (username) ->
            resolver().get("proxy.friends.unfriend.not_friends", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> UNFRIEND_SUCCESS = (username) ->
            resolver().get("proxy.friends.unfriend.success", LanguageResolver.tagHandler("username", username));
    public final static ParameterizedMessage1<String> FRIEND_REQUEST_ALREADY_FRIENDS = (username) ->
            resolver().get("proxy.friends.request.already_friends", LanguageResolver.tagHandler("username", username));
    public final static Component FRIEND_MESSAGING_NO_SELF_MESSAGING = resolver().get("proxy.friends.messaging.no_self_messaging");
    public final static Component FRIEND_MESSAGING_ONLY_FRIENDS = resolver().get("proxy.friends.messaging.only_friends");
    public final static Component FRIEND_MESSAGING_REPLY = text(resolver().getRaw("proxy.friends.messaging.reply"));
    public final static Component MAX_FRIENDS_REACHED = resolver().get("proxy.friends.max_friends_reached");
    public final static ParameterizedMessage1<Integer> FRIENDS_JOIN_MESSAGE_EMPTY = (friend_count) -> join(
            newlines(),
            resolver().getArray(
                    "proxy.friends.join_message_empty",
                    LanguageResolver.tagHandler("friend_count", friend_count)
            )
    );
    public final static ParameterizedMessage1<List<IFriendRequest>> FRIENDS_JOIN_MESSAGE = (requests) -> {
        if(requests.size() > 8)
            return FRIENDS_JOIN_MESSAGE_EMPTY.build(requests.size());

        try {
            AtomicReference<String> from = new AtomicReference<>("");
            requests.forEach(request -> {
                try {
                    from.set(from + ", " + request.sender().username());
                } catch (Exception ignore) {}
            });

            return join(
                    newlines(),
                    resolver().getArray(
                            "proxy.friends.join_message_countable",
                            LanguageResolver.tagHandler("friend_count", requests.size()),
                            LanguageResolver.tagHandler("friend_requests", from.get())
                    )
            );
        } catch (Exception ignore) {
            return FRIENDS_JOIN_MESSAGE_EMPTY.build(requests.size());
        }
    };
    public final static String FRIEND_INJECTED_MAXED = resolver().getRaw("proxy.friends.injected_error.maxed");
    public final static String FRIEND_INJECTED_ACKNOWLEDGED = resolver().getRaw("proxy.friends.injected_error.acknowledged");
    public final static String FRIEND_INJECTED_INTERNAL_ERROR = resolver().getRaw("proxy.friends.injected_error.internal_error");

    public final static Component NO_ONLINE_FRIENDS = resolver().get("proxy.friends.no_online_friends");
    public final static Component ONLINE_FRIENDS = resolver().get("proxy.friends.online_friends");

    public final static Component UNFRIEND_USAGE = text(USAGE+": /unfriend <username>",RED);
    public final static Component FM_USAGE = text(USAGE+": /fm <username> <message>",RED);

    public final static ParameterizedMessage1<String> PING = uuidOrDisplayName -> text(
            resolver().get("proxy.console_icons.ping") + " " + uuidOrDisplayName
    );

    public final static ParameterizedMessage2<String, String> REGISTRATION_REQUEST = (uuidOrDisplayName, familyName) -> text(
            uuidOrDisplayName + " " + resolver().get("proxy.console_icons.attempting_registration") +" "+familyName
    );

    public final static ParameterizedMessage2<String, String> REGISTERED = (uuidOrDisplayName, familyName) -> text(
            uuidOrDisplayName + " " + resolver().get("proxy.console_icons.registered") +" "+familyName
    );

    public final static ParameterizedMessage2<String, String> ERROR = (uuidOrDisplayName, familyName) -> text(
            uuidOrDisplayName + " " + resolver().get("proxy.console_icons.error") +" "+familyName
    );

    public final static ParameterizedMessage2<String, String> UNREGISTRATION_REQUEST = (uuidOrDisplayName, familyName) -> text(
            uuidOrDisplayName + " " + resolver().get("proxy.console_icons.attempting_unregistration") +" "+familyName
    );

    public final static ParameterizedMessage2<String, String> UNREGISTERED = (uuidOrDisplayName, familyName) -> text(
            uuidOrDisplayName + " " + resolver().get("proxy.console_icons.unregistered") +" "+familyName
    );

    public final static ParameterizedMessage1<IFamily> FAMILY_BALANCING = family -> text(
            family.id() + " " + resolver().get("proxy.console_icons.family_balancing")
    );
}
