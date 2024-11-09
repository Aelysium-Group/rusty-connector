package group.aelysium.rustyconnector.plugin.common.lang;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.errors.ErrorRegistry;
import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.common.lang.LangLibrary;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.common.plugins.Plugin;
import group.aelysium.rustyconnector.proxy.magic_link.WebSocketMagicLink;
import group.aelysium.rustyconnector.proxy.util.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

public class CommonLang {
    public static JoinConfiguration newlines() {
        return JoinConfiguration.separator(newline());
    }

    @Lang("rustyconnector-finished")
    public static final Component finished = text("Finished!", DARK_AQUA);
    @Lang("rustyconnector-waiting")
    public static final Component waiting = text("Working on it...", BLUE);

    @Lang("rustyconnector-border")
    public static final Component border = Component.text("-------------------------------------------------------------------------------------------------", DARK_BLUE);

    @Lang("rustyconnector-unknownCommand")
    public static final String unknownCommand = "Unknown command. Type \"/help\" for help.";

    @Lang("rustyconnector-noPermission")
    public static final String noPermission = "You do not have permission to do this.";

    @Lang("rustyconnector-internalError")
    public static final String internalError = "There was an internal error while trying to complete that request.";

    @Lang("rustyconnector-pluginList")
    public static Component moduleReloadList(Set<String> validModules) {
        return RC.Lang("rustyconnector-box").generate(
            join(
                    newlines(),
                    text("Please provide the name of the module you want to see details for. Valid options are:", GRAY),
                    text(String.join(", ", validModules), BLUE),
                    text("If a module has sub-modules, you can view those using using `module.submodule.submodule`.", GRAY),
                    space(),
                    text("rc module <target_module>", BLUE),
                    text("Returns details for specific modules.", DARK_GRAY),
                    space(),
                    text("rc reload <target_module>", BLUE),
                    text("Reloads a specific module.", DARK_GRAY)
            )
        );
    }

    @Lang("rustyconnector-exception")
    public static Component exception(Throwable e) {
        List<Component> stackTrace = new ArrayList<>();

        AtomicReference<Throwable> current = new AtomicReference<>(e);
        while(current.get() != null) {
            if(current.get().getMessage() != null) stackTrace.add(text(current.get().getMessage(), BLUE));
            stackTrace.add(text(e.getClass().getName(), BLUE));
            stackTrace.addAll(Arrays.stream(current.get().getStackTrace()).map(s->text("        "+s.toString(), BLUE)).toList());
            current.set(current.get().getCause());
        }

        return RC.Lang("rustyconnector-box").generate(
                join(
                    newlines(),
                    stackTrace
                )
        );
    }

    @Lang("rustyconnector-error")
    public static Component error(Error error) {
        try {
            List<Component> extras = new ArrayList<>();

            ZonedDateTime zonedDateTime = error.createdAt().atZone(ZoneId.systemDefault());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = zonedDateTime.format(formatter);

            extras.add(
                    join(
                            newlines(),
                            text("RustyConnector", BLUE).append(text(" (" + error.uuid() + ") [" + formattedDateTime +"]", DARK_GRAY)),
                            text(error.message(), GRAY)
                    )
            );

            Component hintOrSolution = space();
            if (error.hint() != null)
                hintOrSolution = hintOrSolution.appendNewline().append(text("Hint: ", BLUE).append(text(error.hint(), GRAY)));
            if (error.solution() != null)
                hintOrSolution = hintOrSolution.appendNewline().append(text("Solution: ", BLUE).append(text(error.solution(), GRAY)));
            extras.add(hintOrSolution);

            if (error.throwable() != null) error.causedBy(error.throwable().getClass().getSimpleName());
            if (!error.details().isEmpty()) {
                extras.add(text("Details: ", BLUE));
                extras.add(join(
                        newlines(),
                        error.details().stream().map(e ->
                                text(" • ", DARK_GRAY)
                                        .append(text(e.key + ": ", BLUE)
                                                .append(typedValue(e.value))
                                        )
                        ).toList()
                ));
            }

            return join(
                    newlines(),
                    space(),
                    space(),
                    join(
                            newlines(),
                            extras
                    ),
                    space()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Component.space();
    }

    @Lang("rustyconnector-keyValue")
    public static Component keyValueLang(String key, Object value) {
        return text(" • "+key+": ", DARK_GRAY).append(RC.Lang("rustyconnector-typedValue").generate(value));
    }

    // Exists as a shorthand way to use the lang entry "rustyconnector-keyValue" cause the nature of that lang value adds a lot of bloat to other places trying to use it.
    public static Component keyValue(String key, Object value) {
        return RC.Lang("rustyconnector-keyValue").generate(key, value);
    }

    @Lang("rustyconnector-typedValue")
    public static Component typedValue(Object value) {
        Class<?> clazz = value.getClass();
        if(Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz) ||
                Double.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz) ||
                Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz) ||
                Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)
        )
            return text(value.toString(), YELLOW);
        if(Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz))
            return text(value.toString(), BLUE);
        if(String.class.isAssignableFrom(clazz))
            return text("\""+value+"\"", GOLD);
        if(Component.class.isAssignableFrom(clazz))
            return (Component) value;
        return text(value.toString(), WHITE);
    }

    @Lang("rustyconnector-wordmark")
    public static Component wordmark(Version version) {// font: ANSI Shadow
        Component versionComponent = space();

        if(version != null && !version.equals(""))
            versionComponent = versionComponent.append(text("Version "+version, DARK_AQUA));

        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text(" /███████                        /██", BLUE),
                        text("| ██__  ██                      | ██", BLUE),
                        text("| ██  \\ ██ /██   /██  /███████ /██████   /██   /██", BLUE),
                        text("| ███████/| ██  | ██ /██_____/|_  ██_/  | ██  | ██", BLUE),
                        text("| ██__  ██| ██  | ██|  ██████   | ██    | ██  | ██", BLUE),
                        text("| ██  \\ ██| ██  | ██ \\____  ██  | ██ /██| ██  | ██", BLUE),
                        text("| ██  | ██|  ██████/ /███████/  |  ████/|  ███████", BLUE),
                        text("|__/  |__/ \\______/ |_______/    \\___/   \\____  ██", BLUE),
                        text("                                         /██  | ██  ", BLUE).append(versionComponent),
                        text("                                        |  ██████/", BLUE),
                        text("  /██████                                \\______/             /██", BLUE),
                        text(" /██__  ██                                                    | ██", BLUE),
                        text("| ██  \\__/  /██████  /███████  /███████   /██████   /███████ /██████    /██████   /██████", BLUE),
                        text("| ██       /██__  ██| ██__  ██| ██__  ██ /██__  ██ /██_____/|_  ██_/   /██__  ██ /██__  ██", BLUE),
                        text("| ██      | ██  \\ ██| ██  \\ ██| ██  \\ ██| ████████| ██        | ██    | ██  \\ ██| ██  \\__/", BLUE),
                        text("| ██    ██| ██  | ██| ██  | ██| ██  | ██| ██_____/| ██        | ██ /██| ██  | ██| ██", BLUE),
                        text("|  ██████/|  ██████/| ██  | ██| ██  | ██|  ███████|  ███████  |  ████/|  ██████/| ██", BLUE),
                        text(" \\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/", BLUE),
                        space(),
                        space(),
                        text("Developed by Aelysium | Juice", DARK_AQUA),
                        text("Use: `/rc` to get started", DARK_AQUA)
                )
        );
    };

    @Lang("rustyconnector-box")
    public static Component box(Component component) {
        return join(
                newlines(),
                space(),
                RC.Lang("rustyconnector-border").generate(),
                space(),
                component,
                space(),
                RC.Lang("rustyconnector-border").generate()
        );
    }

    @Lang("rustyconnector-headerBox")
    public static Component headerBox(@NotNull String header, @NotNull Component content) {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        RC.P.Lang().asciiAlphabet().generate(header, BLUE),
                        space(),
                        RC.Lang("rustyconnector-border").generate(),
                        space(),
                        content
                )
        );
    }

    @Lang("rustyconnector-message")
    public static Component message(Packet.Remote message) {
        return RC.Lang("rustyconnector-headerBox").generate(
                "Message",
                join(
                        newlines(),
                        text("Status: " + message.status().name(), message.status().color()),
                        text("Reason: " + message.statusMessage(), message.status().color()),
                        space(),
                        text("ID: ", message.status().color()).append(text(message.id().toString(), GRAY)),
                        text("Timestamp: ", message.status().color()).append(text(message.received().toString(), GRAY)),
                        text("Contents: ", message.status().color()).append(text(message.toString(), GRAY))
                )
        );
    }

    @Lang("rustyconnector-messages")
    public static Component messages(List<Packet.Remote> packets) {
        return join(
                newlines(),
                packets.stream().map(packet -> join(
                        newlines(),
                        RC.Lang("rustyconnector-border").generate(),
                        text("Status: " + packet.status().name(), packet.status().color()),
                        text("Reason: " + packet.statusMessage(), packet.status().color()),
                        space(),
                        text("ID: ", packet.status().color()).append(text(packet.id().toString(), GRAY)),
                        text("Timestamp: ", packet.status().color()).append(text(packet.received().toString(), GRAY)),
                        text("Contents: ", packet.status().color()).append(text(packet.toString(), GRAY))
                )).toList()
        );
    };

    @Lang("rustyconnector-sendUsage")
    public static Component sendUsage() {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text("rc send <username> <family_name>", BLUE),
                        text("Sends the user to the specific family!", DARK_GRAY),
                        space(),
                        text("rc send <username> server <server_uuid>", BLUE),
                        text("Sends the user to the specific server!", DARK_GRAY)
                )
        );
    }

    @Lang("rustyconnector-pluginAlreadyStarted")
    public static Component pluginAlreadyStarted() {
        return text("This plugin is already running!", BLUE);
    }

    @Lang("rustyconnector-pluginAlreadyStopped")
    public static Component pluginAlreadyStopped() {
        return text("This plugin is already stopped!", BLUE);
    }

    @Lang("rustyconnector-details")
    public static Component details(Plugin plugin) {
        return join(
                newlines(),
                space(),
                space(),
                RC.Lang().asciiAlphabet().generate(plugin.name(), BLUE),
                text(plugin.description(), GRAY),
                space(),
                text("Details:", DARK_GRAY),
                plugin.details(),
                space()
        );
    }

    @Lang("rustyconnector-langLibraryDetails")
    public static Component langLibraryDetails(LangLibrary langLibrary) {
        return join(
                newlines(),
                keyValue("ASCII Alphabet - Supported Characters", "'"+String.join("', '", langLibrary.asciiAlphabet().supportedCharacters().stream().map(c -> c.toString()).toList())+"'"),
                keyValue("Registered Nodes", String.join(", ", langLibrary.langNodes()))
        );
    }

    @Lang("rustyconnector-errorRegistryDetails")
    public static Component errorRegistryDetails(ErrorRegistry errorRegistry) {
        return join(
                newlines(),
                keyValue("Log Errors", errorRegistry.logErrors()),
                keyValue("Cache Size", errorRegistry.cacheSize()),
                keyValue("Error Count", errorRegistry.fetchAll().size())
        );
    }

    @Lang("rustyconnector-eventManagerDetails")
    public static Component eventManagerDetails(EventManager eventManager) {
        return text("No other details exist to show.", DARK_GRAY);
    }

    @Lang("rustyconnector-magicLinkDetails")
    public static Component magicLinkDetails(WebSocketMagicLink magicLink) {
        return join(
                newlines(),
                keyValue("Cached Packets", magicLink.messageCache().size())
        );
    }
}
