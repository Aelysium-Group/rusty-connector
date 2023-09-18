package group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging;

import group.aelysium.rustyconnector.core.lib.lang.Lang;

import java.util.Date;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public interface PaperLang extends Lang {

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

    ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA = isFolia -> {
        if(isFolia) return text("RustyConnector-Folia");
        return text("RustyConnector-Paper");
    };

    ParameterizedMessage1<Boolean> RCNAME_PAPER_FOLIA_LOWER = isFolia -> {
        if(isFolia) return text("rustyconnector-folia");
        return text("rustyconnector-paper");
    };
}
