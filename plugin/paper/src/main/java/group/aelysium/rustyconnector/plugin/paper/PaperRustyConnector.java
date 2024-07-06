package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.common.events.EventManager;
import group.aelysium.rustyconnector.mcloader.Flame;
import group.aelysium.rustyconnector.mcloader.magic_link.MagicLink;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.mc_loader.lang.MCLoaderLang;
import group.aelysium.rustyconnector.toolkit.proxy.util.Version;

import java.net.InetSocketAddress;
import java.util.UUID;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        api.logger().log("Initializing RustyConnector...");

        MagicLink.Tinder magicLink = new MagicLink.Tinder();
        Flame.Tinder tinder = new Flame.Tinder(
                UUID.randomUUID(),
                new Version("0.0.0"),
                new PaperMCLoaderAdapter(),
                "",
                new InetSocketAddress(0),
                magicLink,
                new EventManager()
        );

        MCLoaderLang.WORDMARK_RUSTY_CONNECTOR.send(api.logger(), api.flame().versionAsString());

        RustyConnector.Toolkit.registerMCLoader(tinder.flux());
        try {
            api.logger().log("Registered to bstats!");
        } catch (Exception e) {
            api.logger().log("Failed to registerProxy to bstats!");
        }
    }

    @Override
    public void onDisable() {
        RustyConnector.Toolkit.unregister();
        try {
            RustyConnector.Toolkit.MCLoader().orElseThrow().close();
        } catch (Exception ignore) {}
    }
}