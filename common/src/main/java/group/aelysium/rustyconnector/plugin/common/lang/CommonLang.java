package group.aelysium.rustyconnector.plugin.common.lang;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.common.magic_link.packet.Packet;
import group.aelysium.rustyconnector.proxy.util.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    public static final Component border = Component.text("█████████████████████████████████████████████████████████████████████████████████████████████████");

    @Lang("rustyconnector-unknownCommand")
    public static final String unknownCommand = "Unknown command. Type \"/help\" for help.";

    @Lang("rustyconnector-noPermission")
    public static final String noPermission = "You do not have permission to do this.";

    @Lang("rustyconnector-internalError")
    public static final String internalError = "There was an internal error while trying to complete that request.";
    @Lang("rustyconnector-error")
    public static String error(String error) { return "There was an error while trying to complete that request.\n"+error; }
    @Lang("rustyconnector-missing")
    public static String missing(String what) { return "Unable to find "+what; }

    @Lang("rustyconnector-missing2")
    public static String missing(String what, String identifier) { return "There is no "+what+" with the identifier "+identifier; }

    @Lang("rustyconnector-wordmark")
    public static Component wordmark(Version version) {// font: ANSI Shadow
        Component versionComponent = space();

        if(version != null && !version.equals(""))
            versionComponent = versionComponent.append(text("Version "+version, GREEN));

        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
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
                        space(),
                        space(),
                        text("Developed by Aelysium | Juice"),
                        text("Use: `/rc` to get started", YELLOW)
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
                        RC.P.Lang().asciiAlphabet().generate(header, AQUA),
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
                        text("rc message get <Message ID>", AQUA),
                        text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", DARK_GRAY),
                        space(),
                        text("rc message list <page number>", AQUA),
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
    public Component sendUsage() {
        return RC.Lang("rustyconnector-box").generate(
                join(
                        newlines(),
                        text("send <username> <family_name>", AQUA),
                        text("Sends the user to the specific family!", DARK_GRAY),
                        space(),
                        text("send <username> server <server_uuid>", AQUA),
                        text("Sends the user to the specific server!", DARK_GRAY)
                )
        );
    }
}
