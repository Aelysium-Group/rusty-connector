package group.aelysium.rustyconnector.plugin.paper.central;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.central.PluginAPI;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;
import org.slf4j.Logger;

import java.io.InputStream;
import java.util.function.Function;

public class Tinder extends PluginAPI<BukkitScheduler> {
    private static Tinder instance;
    public static Tinder get() {
        return instance;
    }

    private PaperCommandManager<CommandSender> commandManager;
    private final PaperRustyConnector plugin;
    private Flame flame;
    private final PluginLogger pluginLogger;


    private Tinder(PaperRustyConnector plugin, Logger logger) throws Exception {
        instance = this;
        this.plugin = plugin;
        this.pluginLogger = new PluginLogger(logger);

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
        try {
            this.flame = Flame.fabricateNew(this.plugin);
            return flame;
        } catch (Exception e) {
            this.logger().send(Lang.BOXED_MESSAGE_COLORED.build(e.getMessage(), NamedTextColor.RED));
            throw new RuntimeException(e);
        }
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
    public static Tinder gather(PaperRustyConnector plugin, Logger logger) throws Exception {
        return new Tinder(plugin, logger);
    }
}
