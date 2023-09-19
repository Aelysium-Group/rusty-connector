package group.aelysium.rustyconnector.plugin.paper.central;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.lib.lang.config.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;

public class Tinder extends group.aelysium.rustyconnector.core.central.Tinder<BukkitScheduler> {
    private static Tinder instance;
    public static Tinder get() {
        return instance;
    }

    private PaperCommandManager<CommandSender> commandManager;
    private final PaperRustyConnector plugin;
    private Flame flame;
    private final PluginLogger pluginLogger;
    private LangService lang;


    private Tinder(PaperRustyConnector plugin, PluginLogger logger, LangService lang) throws Exception {
        instance = this;
        this.plugin = plugin;
        this.pluginLogger = logger;
        this.lang = lang;

        this.commandManager = new PaperCommandManager<>(
                plugin,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build(),
                Function.identity(),
                Function.identity()
        );
    }

    /**
     * Ignites a {@link Flame} which effectively starts the RustyConnector kernel.
     * @return A {@link Flame}.
     */
    public Flame ignite() throws RuntimeException {
        this.flame = Flame.fabricateNew(this.plugin, this.lang);
        return flame;
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link Flame} and igniting a new one.
     */
    public void rekindle() {
        this.flame.exhaust(this.plugin);
        this.flame = null;

        this.ignite();
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
    public PluginLogger logger() {
        return this.pluginLogger;
    }

    @Override
    public String dataFolder() {
        return plugin.getDataFolder().getPath();
    }

    @Override
    public LangService lang() {
        return this.lang;
    }

    public Path dataFolderPath() {
        return plugin.getDataFolder().toPath();
    }

    /**
     * Get the paper server
     */
    public Server paperServer() {
        return this.plugin.getServer();
    }

    /**
     * Returns the currently active RustyConnector kernel.
     * @return A {@link Flame}.
     */
    public Flame flame() {
        return this.flame;
    }

    public CoreServiceHandler services() {
        return this.flame.services();
    }

    public PaperCommandManager<CommandSender> commandManager() {
        return commandManager;
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionisedServer");
            return true;
        } catch (ClassNotFoundException ignore) {}
        return false;
    }

    /**
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(PaperRustyConnector plugin, Logger logger) {
        PluginLogger pluginLogger = new PluginLogger(logger);
        try {
            RootLanguageConfig config = new RootLanguageConfig(new File(plugin.getDataFolder(), "language.yml"));
            if (!config.generate(pluginLogger))
                throw new IllegalStateException("Unable to load or create language.yml!");
            config.register();

            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), plugin.getDataFolder().toPath());

            return new Tinder(plugin, pluginLogger, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
