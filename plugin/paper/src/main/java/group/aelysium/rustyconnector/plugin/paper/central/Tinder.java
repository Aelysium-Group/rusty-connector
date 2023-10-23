package group.aelysium.rustyconnector.plugin.paper.central;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.central.Flame;
import group.aelysium.rustyconnector.core.lib.lang.config.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.core.plugin.central.CoreServiceHandler;
import group.aelysium.rustyconnector.plugin.paper.PaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper.PluginLogger;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class Tinder extends group.aelysium.rustyconnector.core.central.Tinder {

    private final PaperCommandManager<CommandSender> commandManager;
    private final PaperRustyConnector plugin;
    private Flame<CoreServiceHandler> flame;
    private final PluginLogger pluginLogger;
    private final LangService lang;


    private Tinder(PaperRustyConnector plugin, PluginLogger logger, LangService lang) throws Exception {
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
     */
    public void ignite() throws RuntimeException {
        this.flame = group.aelysium.rustyconnector.plugin.paper.central.Flame.fabricateNew(this.plugin, this.lang);
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link Flame} and igniting a new one.
     */
    public void rekindle() {
        this.flame.exhaust();
        this.flame = null;

        this.ignite();
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

    @Override
    public void setMaxPlayers(int max) {
        plugin.getServer().setMaxPlayers(max);
    }

    @Override
    public int onlinePlayerCount() {
        return plugin.getServer().getOnlinePlayers().size();
    }

    @Override
    public UUID getPlayerUUID(String name) {
        return plugin.getServer().getOfflinePlayer(name).getUniqueId();
    }

    @Override
    public String getPlayerName(UUID uuid) {
        return plugin.getServer().getOfflinePlayer(uuid).getName();
    }

    @Override
    public boolean isOnline(UUID uuid) {
        return plugin.getServer().getPlayer(uuid) != null;
    }

    @Override
    public void teleportPlayer(UUID uuid, UUID targetUuid) {
        Player client = plugin.getServer().getPlayer(uuid);
        if (client == null) return;

        Player target = plugin.getServer().getPlayer(targetUuid);
        if (target == null) return;

        if (isFolia()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(PaperRustyConnector.getPlugin(PaperRustyConnector.class), () -> {
               client.teleport(target.getLocation());
            }, 0);
        } else {
            client.teleportAsync(target.getLocation());
        }
    }

    @Override
    public void sendMessage(UUID uuid, Component component) {
        Objects.requireNonNull(plugin.getServer().getPlayer(uuid)).sendMessage(component);
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
