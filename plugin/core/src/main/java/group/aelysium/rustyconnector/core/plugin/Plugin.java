package group.aelysium.rustyconnector.core.plugin;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;

public class Plugin {

    private static MCLoaderTinder tinder;

    public static <S extends ServiceHandler> void init(MCLoaderTinder t) {
        tinder = t;
        tinder.ignite();

        tinder.logger().log("Initializing RustyConnector...");
        PluginLang.WORDMARK_RUSTY_CONNECTOR.send(tinder.logger(), tinder.flame().versionAsString());
    }

    public static void disable() {
        tinder.flame().exhaust();
    }

    public static MCLoaderTinder getAPI() {
        return tinder;
    }
}