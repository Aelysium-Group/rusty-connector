package group.aelysium.rustyconnector.plugin.paper;

import group.aelysium.rustyconnector.core.central.PluginRuntime;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.central.PaperLifecycle;
import group.aelysium.rustyconnector.plugin.paper.central.PaperAPI;
import group.aelysium.rustyconnector.plugin.paper.lib.VirtualServerProcessor;
import group.aelysium.rustyconnector.plugin.paper.lib.bstats.Metrics;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class PaperRustyConnector extends JavaPlugin implements Listener, PluginRuntime<PaperAPI> {
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

            if (!lifecycle.start()) {
                this.killPlugin();
                return;
            }

            try {
                new Metrics(this, 17973);
            } catch (Exception e) {
                getAPI().getLogger().log("Failed to register to bstats!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.killPlugin();
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

    private void killPlugin() {
        this.getPluginLoader().disablePlugin(this);
    }
}
