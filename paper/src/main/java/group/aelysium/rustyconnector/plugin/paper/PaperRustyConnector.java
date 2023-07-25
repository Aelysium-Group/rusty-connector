package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.core.central.PluginRuntime;
import group.aelysium.rustyconnector.plugin.paper.central.PaperLifecycle;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.bstats.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener, PluginRuntime {
    private static PaperLifecycle lifecycle;
    public static PaperLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void onEnable() {
        try {
            new PaperAPI(this, this.getSLF4JLogger());
            lifecycle = new PaperLifecycle();

            if (!lifecycle.start()) {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }

            try {
                new Metrics(this, 17973);
                PaperAPI.get().logger().log("Registered to bstats!");
            } catch (Exception e) {
                PaperAPI.get().logger().log("Failed to register to bstats!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            lifecycle.stop();
        } catch (Exception e) {
            PaperAPI.get().logger().log("RustyConnector: " + e.getMessage());
        }
    }
}
