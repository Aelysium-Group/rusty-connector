package group.aelysium.rustyconnector.plugin.paper.central;

import cloud.commandframework.CommandTree;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import group.aelysium.rustyconnector.plugin.paper.lib.VirtualServerProcessor;
import group.aelysium.rustyconnector.plugin.paper.config.DefaultConfig;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.slf4j.Logger;

import java.io.InputStream;
import java.io.SyncFailedException;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

public class PaperAPI extends PluginAPI<BukkitScheduler> {
    private PaperCommandManager<CommandSender> commandManager;
    private final PaperRustyConnector plugin;
    private VirtualServerProcessor virtualProcessor = null;
    private final PluginLogger pluginLogger;


    public PaperAPI(PaperRustyConnector plugin, Logger logger) throws Exception {
        this.plugin = plugin;
        this.pluginLogger = new PluginLogger(logger);

        this.commandManager = new PaperCommandManager<>(
                plugin,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build(),
                Function.identity(),
                Function.identity()
        );
    }

    @Override
    public InputStream getResourceAsStream(String filename)  {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }

    @Override
    public BukkitScheduler getScheduler() {
        return Bukkit.getScheduler();
    }

    @Override
    public PluginLogger getLogger() {
        return this.pluginLogger;
    }

    @Override
    public VirtualServerProcessor getVirtualProcessor() {
        return this.virtualProcessor;
    }

    @Override
    public String getDataFolder() {
        return plugin.getDataFolder().getPath();
    }

    /**
     * Get the paper server
     */
    public Server getServer() {
        return this.plugin.getServer();
    }

    /**
     * Attempt to access the plugin instance directly.
     * @return The plugin instance.
     * @throws SyncFailedException If the plugin is currently running.
     */
    public PaperRustyConnector accessPlugin() throws SyncFailedException {
        if(PaperRustyConnector.getLifecycle().isRunning()) throw new SyncFailedException("You can't get the plugin instance while the plugin is running!");
        return this.plugin;
    }

    public PaperCommandManager<CommandSender> getCommandManager() {
        return commandManager;
    }

    public void configureProcessor(DefaultConfig config) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        if(this.virtualProcessor != null) throw new IllegalAccessException("Attempted to configure the processor while it's already running!");
        this.virtualProcessor = VirtualServerProcessor.init(config);
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionisedServer");
            return true;
        } catch (ClassNotFoundException ignore) {}
        return false;
    }
}
