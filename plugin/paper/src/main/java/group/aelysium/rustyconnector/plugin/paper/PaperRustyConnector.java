package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.core.plugin.Plugin;
import group.aelysium.rustyconnector.plugin.paper.central.Tinder;
import group.aelysium.rustyconnector.plugin.paper.lib.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Tinder api = Tinder.gather(this, this.getSLF4JLogger());
        Plugin.init(api);

        try {
            try {
                new Metrics(this, 17973);
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
        Plugin.disable();
    }
}