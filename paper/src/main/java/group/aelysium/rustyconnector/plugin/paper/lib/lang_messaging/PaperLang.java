package group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging;

import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import net.kyori.adventure.text.Component;

import java.util.Date;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface PaperLang extends Lang {

    Message RC_ROOT_USAGE = () -> join(
            BORDER,
            SPACING,
            WORDMARK_USAGE.build(),
            SPACING,
            BORDER,
            SPACING,
            text("/rc message <message id>", BLUE),
            text("Access a cached message.", GRAY),
            SPACING,
            text("/rc send <username> <family name>", RED),
            text("Send a player from the current server to another family.", GRAY),
            SPACING,
            text("/rc register", RED),
            text("Register this server to the proxy.", GRAY),
            SPACING,
            text("/rc unregister", RED),
            text("Unregister this server from the proxy.", GRAY),
            SPACING,
            BORDER
    );

    Message RC_SEND_USAGE = () -> join(
            BORDER,
            SPACING,
            WORDMARK_USAGE.build(),
            SPACING,
            BORDER,
            SPACING,
            text("/rc send <username> <family name>", RED),
            text("Send a player from the current server to another family.", GRAY),
            SPACING,
            BORDER
    );

    ParameterizedMessage3<Long, Date, String> RC_MESSAGE_GET_MESSAGE = (id, date, contents) -> join(
            BORDER,
            SPACING,
            WORDMARK_MESSAGE.build().color(BLUE),
            SPACING,
            BORDER,
            SPACING,
            text("ID: " + id, BLUE),
            text("Date: " + date, BLUE),
            text("Contents: " + contents, BLUE),
            SPACING,
            BORDER
    );

    ParameterizedMessage1<String> TPA_FAILED_TELEPORT = username -> join(
            text("Something prevented you from teleporting to "+username+"!",RED)
    );

    Component TPA_COMPLETE = text("Teleport completed!",GREEN);

    ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA = isFolia -> {
        if(isFolia) return text("RustyConnector-Folia");
        return text("RustyConnector-Paper");
    };

    ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA_LOWER = isFolia -> {
        if(isFolia) return text("rustyconnector-folia");
        return text("rustyconnector-paper");
    };
}
