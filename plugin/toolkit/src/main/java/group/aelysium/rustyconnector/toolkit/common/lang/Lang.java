package group.aelysium.rustyconnector.toolkit.common.lang;

import group.aelysium.rustyconnector.toolkit.common.cache.CacheableMessage;
import group.aelysium.rustyconnector.toolkit.common.message_cache.ICacheableMessage;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;

public class Lang {
    protected final ASCIIAlphabet asciiAlphabet;

    public Lang(ASCIIAlphabet asciiAlphabet) {
        this.asciiAlphabet = asciiAlphabet;
    }

    public final static String attachedWordmark = "RustyConnector:";
    /*
     * AQUA - For when data is successfully returned or when we send usage info
     * RED - For when an error has occurred.
     * ORANGE/YELLOW - For emphasis or highlighting.
     */
    public final static JoinConfiguration newlines() {
        return JoinConfiguration.separator(newline());
    }

    public String borderPlain() {
        return "█████████████████████████████████████████████████████████████████████████████████████████████████";
    }
    public Component border() {
        return text(borderPlain());
    }

    public String unknown_command() {
        return "Unknown command. Type \"/help\" for help.";
    }
    public String no_permission() {
        return "You do not have permission to do this.";
    }
    public String internal_error() {
        return "There was an internal error while trying to complete your request.";
    }
    public final Component RUSTY_CONNECTOR(Version version) {// font: ANSI Shadow
        Component versionComponent = space();

        if(version != null && !version.equals(""))
            versionComponent = versionComponent.append(text("Version "+version, GREEN));

        return boxed(
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

    public Component boxed(Component component, TextColor borderColor) {
        return join(
                newlines(),
                space(),
                border().color(borderColor),
                space(),
                component,
                space(),
                border().color(borderColor)
        );
    }
    public Component boxed(Component component) {
        return boxed(component, DARK_GRAY);
    }


    public Component headerBox(@NotNull String header, @NotNull Component subheader, @NotNull Component content) {
        return boxed(
                join(
                        newlines(),
                        this.asciiAlphabet.generate(header, AQUA),
                        space(),
                        subheader,
                        space(),
                        border(),
                        space(),
                        content
                )
        );
    }
    public Component headerBox(@NotNull String header, @NotNull Component content) {
        return boxed(
                join(
                        newlines(),
                        this.asciiAlphabet.generate(header, AQUA),
                        space(),
                        border(),
                        space(),
                        content
                )
        );
    }

    public Component usageBox(Component content) {
        return headerBox(
                "Usage",
                join(
                        newlines(),
                        text("Blue commands will return information or data to you! They will not cause changes to be made.", GRAY),
                        text("Orange commands will make the plugin do something. Make sure you know what these commands do before using them", GRAY)
                ),
                content
        );
    }






    public Component messageUsage() {
        return usageBox(
                join(
                        newlines(),
                        text("/rc message get <Message ID>", AQUA),
                        text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", DARK_GRAY),
                        space(),
                        text("/rc message list <page number>", AQUA),
                        text("Lists all currently cached messages! As new messages get cached, older ones will be pushed out of the cache.", DARK_GRAY)
                )
        );
    }

    public Component messageGetUsage() {
        return usageBox(
                join(
                        newlines(),
                        text("/rc message get <Message ID>",AQUA),
                        text("Pulls a message out of the message cache. If a message is to old it might not be available anymore!", DARK_GRAY)
                )
        );
    }


    public Component message(ICacheableMessage message) {
        return headerBox(
                "Message",
                join(
                        newlines(),
                        text("Status: " + message.getSentence().name(), message.getSentence().color()),
                        text("Reason: " + message.getSentenceReason(), message.getSentence().color()),
                        space(),
                        text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY))
                )
        );
    }

    public Component messagePage(List<CacheableMessage> messages, int pageNumber, int maxPages) {
        Component output = text("");
        for (CacheableMessage message : messages) {
            if(!(message.getSentenceReason() == null))
                output = output.append(join(
                        newlines(),
                        border(),
                        space(),
                        text("Status: " + message.getSentence().name(), message.getSentence().color()),
                        text("Reason: " + message.getSentenceReason(), message.getSentence().color()),
                        space(),
                        text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                        space()
                ));
            else
                output = output.append(join(
                        newlines(),
                        border(),
                        space(),
                        text("Status: " + message.getSentence().name(), message.getSentence().color()),
                        space(),
                        text("ID: ", message.getSentence().color()).append(text(message.getSnowflake(), GRAY)),
                        text("Timestamp: ", message.getSentence().color()).append(text(message.getDate().toString(), GRAY)),
                        text("Contents: ", message.getSentence().color()).append(text(message.getContents(), GRAY)),
                        space()
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
                boxed(
                        join(
                                newlines(),
                                text("Pages:"),
                                pageNumbers
                        )
                )
        );
    };

    public Component sendUsage() {
        return usageBox(
                join(
                        newlines(),
                        text("/rc send <username> <family word_id>", GOLD),
                        text("Sends a player from one family to another!", DARK_GRAY),
                        space(),
                        text("/rc send server <username> <server word_id>", GOLD),
                        text("Forces a player to connect to a specific server on the proxy. This bypasses player caps and family whitelists.", DARK_GRAY),
                        text("If you have multiple servers with the same name, this feature may send players to a server other than the one you intended.", DARK_GRAY)
                )
        );
    }
}




