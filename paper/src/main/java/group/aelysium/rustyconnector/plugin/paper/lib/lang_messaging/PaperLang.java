package group.aelysium.rustyconnector.plugin.paper.lib.lang_messaging;

import group.aelysium.rustyconnector.core.lib.data_messaging.cache.CacheableMessage;
import group.aelysium.rustyconnector.core.lib.lang_messaging.ASCIIAlphabet;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.core.lib.util.AddressUtil;
import net.kyori.adventure.text.Component;

import java.util.Date;
import java.util.List;

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
}
