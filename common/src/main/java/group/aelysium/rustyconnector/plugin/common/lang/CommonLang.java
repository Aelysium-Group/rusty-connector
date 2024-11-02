package group.aelysium.rustyconnector.plugin.common.lang;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.errors.Error;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.proxy.util.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;

public class CommonLang {
    public static JoinConfiguration newlines() {
        return JoinConfiguration.separator(newline());
    }

    @Lang("rustyconnector-finished")
    public static final String finished = "Finished!";
    @Lang("rustyconnector-waiting")
    public static final String waiting = "Working on it...";

    @Lang("rustyconnector-border")
    public static final Component border = Component.text("█████████████████████████████████████████████████████████████████████████████████████████████████", DARK_GRAY);

    @Lang("rustyconnector-unknownCommand")
    public static final String unknownCommand = "Unknown command. Type \"/help\" for help.";

    @Lang("rustyconnector-noPermission")
    public static final String noPermission = "You do not have permission to do this.";

    @Lang("rustyconnector-internalError")
    public static final String internalError = "There was an internal error while trying to complete that request.";

    @Lang("rustyconnector-moduleReloadList")
    public static Component moduleReloadList(Set<String> validModules) {
        return Component.join(
                JoinConfiguration.builder().separator(Component.newline()).build(),
                Component.text("Please provide the name of the module you want to reload. Valid options are: Kernel, "+String.join(", ", validModules)),
                Component.text("If you wish to reload specific families, or family specific modules, you can do that under the /family menu.")
        );
    }

    @Lang("rustyconnector-exception")
    public static Component exception(Throwable e) {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        Component.text(e.getMessage() == null ? "No message was provided by this exception" : e.getMessage(), NamedTextColor.BLUE),
                        Component.text(e.getClass().getName(), NamedTextColor.BLUE),
                        join(
                                newlines(),
                                Arrays.stream(e.getStackTrace()).map(t->Component.text("        "+t.toString(), NamedTextColor.BLUE)).toList()
                        )
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
                                                .append(RC.Lang("rustyconnector-typedValue").generate(e.value))
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
                        text("\\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/", BLUE),
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
                RC.Lang("rustyconnector-border").generate().color(DARK_GRAY),
                space(),
                component,
                space(),
                RC.Lang("rustyconnector-border").generate().color(DARK_GRAY)
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

    @Lang("rustyconnector-messageUsage")
    public static Component messageUsage() {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text("rc message get <Message ID>", BLUE),
                        text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", DARK_GRAY),
                        space(),
                        text("rc message list <page number>", BLUE),
                        text("Lists all currently cached messages! As new messages get cached, older ones will be pushed out of the cache.", DARK_GRAY)
                )
        );
    }

    @Lang("rustyconnector-messageGetUsage")
    public static final String messageGetUsage = "/rc message get <Message ID>";

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

    @Lang("rustyconnector-messagePage")
    public static Component messagePage(List<Packet.Remote> packets, int pageNumber, int maxPages) {
        Component output = text("");
        for (Packet.Remote packet : packets)
            output = output.append(join(
                    newlines(),
                    RC.Lang("rustyconnector-border").generate(),
                    text("Status: " + packet.status().name(), packet.status().color()),
                    text("Reason: " + packet.statusMessage(), packet.status().color()),
                    space(),
                    text("ID: ", packet.status().color()).append(text(packet.id().toString(), GRAY)),
                    text("Timestamp: ", packet.status().color()).append(text(packet.received().toString(), GRAY)),
                    text("Contents: ", packet.status().color()).append(text(packet.toString(), GRAY))
            ));

        Component pageNumbers = text("[ ",DARK_GRAY);
        for (int i = 1; i <= maxPages; i++) {
            if(i == pageNumber)
                pageNumbers = pageNumbers.append(text(i+" ",GOLD));
            else
                pageNumbers = pageNumbers.append(text(i+" ",GRAY));
        }
        pageNumbers = pageNumbers.append(text("]",DARK_GRAY));

        return output.append(
                RC.Lang("rustyconnector-box").generate(
                        join(
                                newlines(),
                                RC.Lang("rustyconnector-border").generate(),
                                text("Pages:"),
                                pageNumbers
                        )
                )
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
}
