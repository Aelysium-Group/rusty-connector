package group.aelysium.rustyconnector.plugin.paper;

import com.google.gson.stream.JsonReader;
import group.aelysium.rustyconnector.core.central.PluginRuntime;
import group.aelysium.rustyconnector.plugin.paper.central.PaperLifecycle;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.bstats.Metrics;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener, PluginRuntime {
    private static PaperLifecycle lifecycle;
    private static PaperAPI api;

    public static PaperAPI getAPI() {
        return api;
    }
    public static PaperLifecycle getLifecycle() {
        return lifecycle;
    }

    @Override
    public void onEnable() {
        try {
            api = new PaperAPI(this, this.getSLF4JLogger());
            lifecycle = new PaperLifecycle();

            if (!lifecycle.start()) {
                this.getPluginLoader().disablePlugin(this);
                return;
            }

            try {
                new Metrics(this, 17973);
                getAPI().getLogger().log("Registered to bstats!");
            } catch (Exception e) {
                getAPI().getLogger().log("Failed to register to bstats!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.getPluginLoader().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        try {
            lifecycle.stop();
        } catch (Exception e) {
            getAPI().getLogger().log("RustyConnector: " + e.getMessage());
        }
    }
}
