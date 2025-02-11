package group.aelysium.rustyconnector.plugin.serverCommon;

import group.aelysium.rustyconnector.RC;
import group.aelysium.rustyconnector.common.lang.Lang;
import group.aelysium.rustyconnector.plugin.common.lang.CommonLang;
import group.aelysium.rustyconnector.proxy.util.AddressUtil;
import group.aelysium.rustyconnector.server.ServerKernel;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.Component.*;

public class ServerLang extends CommonLang {
    public ServerLang() {}

    @Lang("rustyconnector-kernelDetails")
    public static Component usage(ServerKernel kernel) {
        return RC.Lang("rustyconnector-headerBox").generate(
                "server",
                join(
                        newlines(),
                        text("Details:", DARK_GRAY),
                        kernel.details(),
                        empty(),
                        text("Commands:", DARK_GRAY),
                        text("rc send <player> <target> [flags]", BLUE),
                        text("Send a player to a family or server.", DARK_GRAY),
                        space(),
                        text("rc packets ['clear' | <packet_id>]", BLUE),
                        text("Access recently sent MagicLink packets.", DARK_GRAY),
                        space(),
                        text("rc reload", BLUE),
                        text("Reload RustyConnector.", DARK_GRAY),
                        space(),
                        text("rc plugins [plugin_node] ['start' | 'stop' | 'reload']", BLUE),
                        text("Get details for RustyConnector modules.", DARK_GRAY),
                        space(),
                        text("rc errors [error_uuid]", BLUE),
                        text("Fetches the recent errors thrown by RustyConnector.", DARK_GRAY),
                        empty()
                )
        );
    }

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
                text(reason, RED),
                text("Waiting "+delayAmount+" "+delayUnit+" before trying again...", GRAY)
        );
    }
}
