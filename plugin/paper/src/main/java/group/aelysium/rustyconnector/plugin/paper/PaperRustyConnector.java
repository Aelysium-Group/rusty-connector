package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.core.lib.lang.Lang;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.bstats.Metrics;
import group.aelysium.rustyconnector.plugin.paper.lib.lang.PaperLang;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    private Tinder tinder;

    @Override
    public void onEnable() {
        try {
            this.tinder = Tinder.gather(this, this.getSLF4JLogger());
            this.tinder.ignite();
            Tinder.get().logger().log("Initializing RustyConnector...");

            PaperLang.WORDMARK_RUSTY_CONNECTOR.send(this.tinder.logger(), this.tinder.flame().version());

            try {
                new Metrics(this, 17973);
                Tinder.get().logger().log("Registered to bstats!");
            } catch (Exception e) {
                Tinder.get().logger().log("Failed to register to bstats!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            this.tinder.flame().exhaust(this);
        } catch (Exception e) {
            Tinder.get().logger().log("RustyConnector: " + e.getMessage());
        }
    }
}
