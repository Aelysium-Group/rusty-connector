package group.aelysium.rustyconnector.plugin.paper.lang;

import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;

public class PaperLang extends CommonLang {
    public PaperLang() {}

    @Lang("rustyconnector-magicLinkHandshake")
    public static Component magicLink() {
        return join(
                newlines(),
                space(),
                text("              /(¯`·._.·´¯`·._.·´¯`·._.·´¯`·._.·´¯)\\"),
                text("          --<||(     MAGIC LINK -- CONNECTED     )||>--"),
                text("              \\(_.·´¯`·._.·´¯`·._.·´¯`·._.·´¯`·._)/")
        ).color(DARK_PURPLE);
    }

    @Lang("rustyconnector-consoleOnly")
    public static final String consoleOnly = "This command can only be executed from the console.";

    public static Component paperFolia(boolean isFolia) {
        if(isFolia) return text("RustyConnector-Folia");
        return text("RustyConnector-Paper");
    };

    public static Component paperFoliaLowercase(boolean isFolia) {
        if(isFolia) return text("rustyconnector-folia");
        return text("rustyconnector-paper");
    };

    @Lang("rustyconnector-magicLinkHandshakeFailure")
    public static Component magicLinkHandshakeFailure(String reason, int delayAmount, TimeUnit delayUnit) {
        return join(
                newlines(),
                text(reason, NamedTextColor.RED),
                text("Waiting "+delayAmount+" "+delayUnit+" before trying again...", NamedTextColor.GRAY)
        );
    }
}
