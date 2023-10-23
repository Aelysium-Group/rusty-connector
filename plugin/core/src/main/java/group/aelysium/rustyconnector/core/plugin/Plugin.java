package group.aelysium.rustyconnector.core.plugin;

import group.aelysium.rustyconnector.core.central.Tinder;
import group.aelysium.rustyconnector.core.lib.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;

public class Plugin {

    private static Tinder tinder;

    public static <S extends ServiceHandler> void init(Tinder t) {
        tinder = t;
        tinder.ignite();

        tinder.logger().log("Initializing RustyConnector...");
        PluginLang.WORDMARK_RUSTY_CONNECTOR.send(tinder.logger(), tinder.flame().versionAsString());
    }

    public static void disable() {
        tinder.flame().exhaust();
    }

    public static Tinder getAPI() {
        return tinder;
    }
}