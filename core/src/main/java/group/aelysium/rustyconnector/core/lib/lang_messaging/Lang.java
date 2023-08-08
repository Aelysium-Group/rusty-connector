package group.aelysium.rustyconnector.core.lib.lang_messaging;

import group.aelysium.rustyconnector.core.central.PluginLogger;
import group.aelysium.rustyconnector.core.lib.data_transit.cache.CacheableMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.List;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

/**
 * Thank you to https://github.com/LuckPerms/LuckPerms for inspiring this implementation.
 *
 * Some text generated using: https://patorjk.com/software/taag/
 */
public interface Lang {
    String attachedWordmark = "RustyConnector:";

    /*
     * AQUA - For when data is successfully returned or when we send usage info
     * RED - For when an error has occurred.
     * ORANGE/YELLOW - For emphasis or highlighting.
     */
    static JoinConfiguration newlines() {
        return JoinConfiguration.separator(newline());
    }

    Component BORDER = text("█████████████████████████████████████████████████████████████████████████████████████████████████", DARK_GRAY);
    ParameterizedMessage1<NamedTextColor> COLORED_BORDER = color -> text("█████████████████████████████████████████████████████████████████████████████████████████████████", color);

    Component SPACING = text("");

    Component UNKNOWN_COMMAND = text("Unknown command. Type \"/help\" for help.",WHITE);

    Message WORDMARK_USAGE = () -> ASCIIAlphabet.generate("usage");

    Message WORDMARK_MESSAGE = () -> ASCIIAlphabet.generate("message");

    ParameterizedMessage1<String> WORDMARK_RUSTY_CONNECTOR = (version) -> {// font: ANSI Shadow
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
                text("Developed by Aelysium | Nathan M.", YELLOW),
                text("Use: `/rc` to get started", YELLOW),
                SPACING,
                BORDER
        );
    };

    ParameterizedMessage1<Component> BOXED_MESSAGE = (message) -> join(
            newlines(),
            SPACING,
            BORDER,
            SPACING,
            message,
            SPACING,
            BORDER,
            SPACING
    );
    ParameterizedMessage2<Component, NamedTextColor> BOXED_MESSAGE_COLORED = (message, color) -> join(
            newlines(),
            SPACING,
            BORDER.color(color),
            SPACING,
            message.color(color),
            SPACING,
            BORDER.color(color),
            SPACING
    );

    ParameterizedMessage1<CacheableMessage> CACHED_MESSAGE = (message) -> join(
                    newlines(),
                    BORDER,
                    text("Status: " + message.getSentence().name(), message.getSentence().color()),
                    text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                    text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                    text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                    BORDER
            );

    ParameterizedMessage3<List<CacheableMessage>,Integer, Integer> RC_MESSAGE_PAGE = (messages, pageNumber, maxPages) -> {
        Component output = text("");
        for (CacheableMessage message : messages) {
            if(!(message.getSentenceReason() == null))
                output = output.append(join(
                        newlines(),
                        BORDER,
                        SPACING,
                        text("Status: " + message.getSentence().name(), message.getSentence().color()),
                        text("Reason: " + message.getSentenceReason(), message.getSentence().color()),
                        SPACING,
                        text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                        SPACING
                ));
            else
                output = output.append(join(
                        newlines(),
                        BORDER,
                        SPACING,
                        text("Status: " + message.getSentence().name(), message.getSentence().color()),
                        SPACING,
                        text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
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
                        text("Pages:"),
                        pageNumbers,
                        SPACING,
                        BORDER
                )
        );
    };

    interface Message {
        Component build();

        default void send(PluginLogger sender) {
            sender.send(
                    join(
                            newlines(),
                            text(attachedWordmark),
                            build()
                    )
            );
        }
    }
    interface ParameterizedMessage1<A1> {
        Component build(A1 arg1);

        default void send(PluginLogger sender, A1 arg1) {
            sender.send(
                    join(
                            JoinConfiguration.separator(newline()),
                            text(attachedWordmark),
                            build(arg1)
                    )
            );
        }
    }
    interface ParameterizedMessage2<A1, A2> {
        Component build(A1 arg1, A2 arg2);

        default void send(PluginLogger sender, A1 arg1, A2 arg2) {
            sender.send(
                    join(
                            newlines(),
                            text(attachedWordmark),
                            build(arg1, arg2)
                    )
            );
        }
    }
    interface ParameterizedMessage3<A1, A2, A3> {
        Component build(A1 arg1, A2 arg2, A3 arg3);

        default void send(PluginLogger sender, A1 arg1, A2 arg2, A3 arg3) {
            sender.send(
                    join(
                            newlines(),
                            text(attachedWordmark),
                            build(arg1, arg2, arg3)
                    )
            );
        }
    }
    interface ParameterizedMessage4<A1, A2, A3, A4> {
        Component build(A1 arg1, A2 arg2, A3 arg3, A4 arg4);

        default void send(PluginLogger sender, A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
            sender.send(
                    join(
                            newlines(),
                            text(attachedWordmark),
                            build(arg1, arg2, arg3, arg4)
                    )
            );
        }
    }
}




