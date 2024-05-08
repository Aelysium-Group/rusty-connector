package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.rustyconnector.core.mcloader.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Tinder api = Tinder.gather(this, this.getSLF4JLogger());

        try {
            TinderAdapterForCore.init(api);

            api.logger().log("Initializing RustyConnector...");
            api.ignite(Bukkit.getPort());
            MCLoaderLang.WORDMARK_RUSTY_CONNECTOR.send(api.logger(), api.flame().versionAsString());

            RustyConnector.Toolkit.register(api);
            try {
                api.logger().log("Registered to bstats!");
            } catch (Exception e) {
                api.logger().log("Failed to register to bstats!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        RustyConnector.Toolkit.unregister();
        Tinder.get().exhaust();
    }
}