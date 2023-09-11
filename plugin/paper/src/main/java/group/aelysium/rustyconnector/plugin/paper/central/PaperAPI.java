package group.aelysium.rustyconnector.plugin.paper.central;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import group.aelysium.rustyconnector.plugin.paper.lib.Core;
import group.aelysium.rustyconnector.plugin.paper.lib.CoreServiceHandler;
import group.aelysium.rustyconnector.plugin.paper.lib.database.RedisSubscriber;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.SyncFailedException;
import java.util.function.Function;

public class PaperAPI extends PluginAPI<BukkitScheduler> {
    private static PaperAPI instance;
    public static PaperAPI get() {
        return instance;
    }

    private PaperCommandManager<CommandSender> commandManager;
    private final PaperRustyConnector plugin;
    private Core core = BootManager.init();
    private final PluginLogger pluginLogger;


    public PaperAPI(PaperRustyConnector plugin, Logger logger) throws Exception {
        instance = this;
        this.plugin = plugin;
        this.pluginLogger = new PluginLogger(logger);

        this.commandManager = new PaperCommandManager<>(
                plugin,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build(),
                Function.identity(),
                Function.identity()
        );

        Lang.WORDMARK_RUSTY_CONNECTOR.send(logger, api.version());
    }

    @Override
    public InputStream resourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public BukkitScheduler scheduler() {
        return Bukkit.getScheduler();
    }

    @Override
    public String version() {
        return null;
    }

    @Override
    public PluginLogger logger() {
        return this.pluginLogger;
    }

    @Override
    public String dataFolder() {
        return plugin.getDataFolder().getPath();
    }

    /**
     * Get the paper server
     */
    public Server paperServer() {
        return this.plugin.getServer();
    }

    public CoreServiceHandler services() {
        return this.bootManager.services();
    }

    public void killServices() {
        this.bootManager.kill();
    }

    /**
     * Attempt to access the plugin instance directly.
     * @return The plugin instance.
     * @throws SyncFailedException If the plugin is currently running.
     */
    public PaperRustyConnector accessPlugin() throws SyncFailedException {
        if(PaperRustyConnector.lifecycle().isRunning()) throw new SyncFailedException("You can't get the plugin instance while the plugin is running!");
        return this.plugin;
    }

    public PaperCommandManager<CommandSender> commandManager() {
        return commandManager;
    }

    public void configureProcessor(DefaultConfig config) throws IllegalAccessException {
        if(this.bootManager != null) throw new IllegalAccessException("Attempted to configure the processor while it's already running!");
        this.bootManager = BootManager.init(config);
        this.bootManager.services().redisService().connection().orElseThrow().startListening(RedisSubscriber.class);
        this.bootManager.services().magicLinkService().startHeartbeat();
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionisedServer");
            return true;
        } catch (ClassNotFoundException ignore) {}
        return false;
    }
}
