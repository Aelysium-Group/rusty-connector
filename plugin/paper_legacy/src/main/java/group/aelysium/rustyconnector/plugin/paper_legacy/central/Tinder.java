package group.aelysium.rustyconnector.plugin.paper_legacy.central;

import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.mcloader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.plugin.paper_legacy.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper_legacy.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.paper_legacy.events.OnPlayerLeave;
import group.aelysium.rustyconnector.plugin.paper_legacy.events.OnPlayerPreLogin;
import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.core.lib.lang.config.RootLanguageConfig;
import group.aelysium.rustyconnector.plugin.paper_legacy.LegacyPaperRustyConnector;
import group.aelysium.rustyconnector.plugin.paper_legacy.PluginLogger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;

public class Tinder extends MCLoaderTinder {
    protected static Tinder instance;
    public static Tinder get() {
        return instance;
    }
    private final PaperCommandManager<CommandSender> commandManager;
    private final LegacyPaperRustyConnector plugin;
    private final PluginLogger pluginLogger;
    private final LangService lang;


    private Tinder(LegacyPaperRustyConnector plugin, PluginLogger logger, LangService lang) throws Exception {
        super(logger, lang);
        this.plugin = plugin;
        this.pluginLogger = logger;
        this.lang = lang;

        this.commandManager = new PaperCommandManager<>(
                plugin,
                AsynchronousCommandExecutionCoordinator.<CommandSender>builder().build(),
                Function.identity(),
                Function.identity()
        );

        instance = this;
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

    @Override
    public void setMaxPlayers(int max) { logger().error("Changing the max players is not supported on legacy paper."); }

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
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(LegacyPaperRustyConnector.getPlugin(LegacyPaperRustyConnector.class), () -> {
               client.teleport(target.getLocation());
            }, 0);
        } else {
            client.teleport(target.getLocation());
        }
    }

    @Override
    public void sendMessage(UUID uuid, Component component) {
        Objects.requireNonNull(plugin.getServer().getPlayer(uuid)).sendMessage(PlainTextComponentSerializer.plainText().serialize(component));
    }

    /**
     * Get the paper server
     */
    public Server paperServer() {
        return this.plugin.getServer();
    }

    @Override
    public Object server() {
        return this.paperServer();
    }

    public PaperCommandManager<CommandSender> commandManager() {
        return commandManager;
    }

    public boolean isFolia() {
        return false;
    }

    @Override
    public void ignite(int port) throws RuntimeException {
        super.ignite(port);

        CommandRusty.create(this.commandManager());

        this.paperServer().getPluginManager().registerEvents(new OnPlayerJoin(), plugin);
        this.paperServer().getPluginManager().registerEvents(new OnPlayerLeave(), plugin);
        this.paperServer().getPluginManager().registerEvents(new OnPlayerPreLogin(), plugin);
    }

    /**
     * Creates new {@link Tinder} based on the gathered resources.
     */
    public static Tinder gather(LegacyPaperRustyConnector plugin, Logger logger) {
        PluginLogger pluginLogger = new PluginLogger(logger);
        try {
            RootLanguageConfig config = RootLanguageConfig.construct(plugin.getDataFolder().toPath());

            LangService langService = LangService.resolveLanguageCode(config.getLanguage(), plugin.getDataFolder().toPath());

            return new Tinder(plugin, pluginLogger, langService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
