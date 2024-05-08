package group.aelysium.rustyconnector.core.mcloader.lib.lang;

import group.aelysium.rustyconnector.core.lib.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.lang.ASCIIAlphabet;
import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.core.lib.lang.LanguageResolver;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Date;
import java.util.List;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class MCLoaderLang extends Lang {
    public static LanguageResolver resolver() {
        return (LanguageResolver) TinderAdapterForCore.getTinder().lang().resolver();
    }

    public final static String ID = resolver().getRaw("core.single_word.id");
    public final static String CONTENTS = resolver().getRaw("core.single_word.contents");
    public final static String DATE = resolver().getRaw("core.single_word.date");
    public final static String REASON = resolver().getRaw("core.single_word.reason");
    public final static String TIMESTAMP = resolver().getRaw("core.single_word.timestamp");
    public final static String PAGES = resolver().getRaw("core.single_word.pages");

    public final static String STATUS = resolver().getRaw("core.single_word.status");

    public final static Component BORDER = text("█████████████████████████████████████████████████████████████████████████████████████████████████", DARK_GRAY);

    public final static Component SPACING = text("");

    public final static Message WORDMARK_USAGE = () -> ASCIIAlphabet.generate("usage");

    public final static Message WORDMARK_MESSAGE = () -> ASCIIAlphabet.generate("message");

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

    public final static Message MAGIC_LINK = () -> join(
            newlines(),
            SPACING,
            text("              /(¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯)\\"),
            text("          --<||(     MAGIC LINK -- CONNECTED     )||>--"),
            text("              \\(_.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._)/")
    ).color(DARK_PURPLE);

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

    public final static Message RC_SEND_USAGE = () -> join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_USAGE.build(),
            SPACING,
            BORDER,
            SPACING,
            text("/rc send <username> <family name>", RED),
            resolver().get("mcloader.send.usage"),
            SPACING,
            BORDER
    );

    public final static ParameterizedMessage3<Long, Date, String> RC_MESSAGE_GET_MESSAGE = (id, date, contents) -> join(
            newlines(),
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.build().color(BLUE),
            SPACING,
            BORDER,
            SPACING,
            text(ID + ": " + id, BLUE),
            text(DATE + ": " + date, BLUE),
            text(CONTENTS + ": " + contents, BLUE),
            SPACING,
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

    public final static ParameterizedMessage1<String> TPA_FAILED_TELEPORT = username ->
            resolver().get("mcloader.tpa.sender_failed_teleport", LanguageResolver.tagHandler("username", username));

    public final static ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA = isFolia -> {
        if(isFolia) return text("RustyConnector-Folia");
        return text("RustyConnector-Paper");
    };

    public final static ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA_LOWER = isFolia -> {
        if(isFolia) return text("rustyconnector-folia");
        return text("rustyconnector-paper");
    };
}
