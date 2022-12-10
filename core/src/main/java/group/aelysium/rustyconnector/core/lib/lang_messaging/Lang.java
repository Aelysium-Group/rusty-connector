package group.aelysium.rustyconnector.core.lib.lang_messaging;

import group.aelysium.rustyconnector.core.lib.model.Server;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.newlines;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_RED;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

/**
 * Thank you to https://github.com/LuckPerms/LuckPerms for inspiring this implementation.
 *
 * Text generated using: https://patorjk.com/software/taag/
 */
public interface Lang {
    /*
     * AQUA - For when data is successfully returned or when we send usage info
     * RED - For when an error has occurred.
     * ORANGE/YELLOW - For emphasis or highlighting.
     */
    Component BORDER = text("█████████████████████████████████████████████████████████████████████████████████████████████████", DARK_GRAY);
    ParameterizedMessage1<NamedTextColor> COLORED_BORDER = color -> text("█████████████████████████████████████████████████████████████████████████████████████████████████", color);

    Component SPACING = text("");

    Message WORDMARK_INFO = () -> ASCIIAlphabet.generate("info");

    Message WORDMARK_USAGE = () -> ASCIIAlphabet.generate("usage");

    Message WORDMARK_CACHED_MESSAGES = () -> join(
            newlines(),
            ASCIIAlphabet.generate("cached"),
            SPACING,
            ASCIIAlphabet.generate("messages")
    );

    Message WORDMARK_MESSAGE = () -> ASCIIAlphabet.generate("message");

    Message WORDMARK_RUSTY_CONNECTOR = () -> // font: ANSI Shadow
            join(
                newlines(),
                BORDER,
                SPACING,
                text(" /███████                        /██"                                                           , AQUA),
                text("| ██__  ██                      | ██"                                                           , AQUA),
                text("| ██  \\ ██ /██   /██  /███████ /██████   /██   /██"                                            , AQUA),
                text("| ███████/| ██  | ██ /██_____/|_  ██_/  | ██  | ██"                                             , AQUA),
                text("| ██__  ██| ██  | ██|  ██████   | ██    | ██  | ██"                                             , AQUA),
                text("| ██  \\ ██| ██  | ██ \\____  ██  | ██ /██| ██  | ██"                                           , AQUA),
                text("| ██  | ██|  ██████/ /███████/  |  ████/|  ███████"                                             , AQUA),
                text("|__/  |__/ \\______/ |_______/    \\___/   \\____  ██"                                          , AQUA),
                text("                                         /██  | ██"                                             , AQUA),
                text("                                        |  ██████/"                                             , AQUA),
                text("  /██████                                \\______/             /██"                             , AQUA),
                text(" /██__  ██                                                    | ██"                             , AQUA),
                text("| ██  \\__/  /██████  /███████  /███████   /██████   /███████ /██████    /██████   /██████"     , AQUA),
                text("| ██       /██__  ██| ██__  ██| ██__  ██ /██__  ██ /██_____/|_  ██_/   /██__  ██ /██__  ██"     , AQUA),
                text("| ██      | ██  \\ ██| ██  \\ ██| ██  \\ ██| ████████| ██        | ██    | ██  \\ ██| ██  \\__/", AQUA),
                text("| ██    ██| ██  | ██| ██  | ██| ██  | ██| ██_____/| ██        | ██ /██| ██  | ██| ██"           , AQUA),
                text("|  ██████/|  ██████/| ██  | ██| ██  | ██|  ███████|  ███████  |  ████/|  ██████/| ██"           , AQUA),
                text("\\______/  \\______/ |__/  |__/|__/  |__/ \\_______/ \\_______/   \\___/   \\______/ |__/"      , AQUA),
                SPACING,
                BORDER,
                SPACING,
                text("Developed by Aelysium | Nathan (SIVIN)", YELLOW),
                text("Use: `/rc` to get started", YELLOW),
                SPACING,
                BORDER
            );

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

    interface Message {
        Component build();

        default void send(Logger sender) {
            sender.send(
                    join(
                            newlines(),
                            text(""),
                            build()
                    )
            );
        }
    }
    interface ParameterizedMessage1<A1> {
        Component build(A1 arg1);

        default void send(Logger sender, A1 arg1) {
            sender.send(
                    join(
                            newlines(),
                            text(""),
                            build(arg1)
                    )
            );
        }
    }
    interface ParameterizedMessage2<A1, A2> {
        Component build(A1 arg1, A2 arg2);

        default void send(Logger sender, A1 arg1, A2 arg2) {
            sender.send(
                    join(
                            newlines(),
                            text(""),
                            build(arg1, arg2)
                    )
            );
        }
    }
    interface ParameterizedMessage3<A1, A2, A3> {
        Component build(A1 arg1, A2 arg2, A3 arg3);

        default void send(Logger sender, A1 arg1, A2 arg2, A3 arg3) {
            sender.send(
                    join(
                            newlines(),
                            text(""),
                            build(arg1, arg2, arg3)
                    )
            );
        }
    }
    interface ParameterizedMessage4<A1, A2, A3, A4> {
        Component build(A1 arg1, A2 arg2, A3 arg3, A4 arg4);

        default void send(Logger sender, A1 arg1, A2 arg2, A3 arg3, A4 arg4) {
            sender.send(
                    join(
                            newlines(),
                            text(""),
                            build(arg1, arg2, arg3, arg4)
                    )
            );
        }
    }
}




