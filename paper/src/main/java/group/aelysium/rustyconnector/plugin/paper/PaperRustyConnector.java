package group.aelysium.rustyconnector.plugin.paper;

import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.core.lib.generic.MessageCache;
import group.aelysium.rustyconnector.plugin.paper.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.paper.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.paper.lib.generic.PaperServer;
import group.aelysium.rustyconnector.plugin.paper.lib.generic.database.Redis;
import group.aelysium.rustyconnector.plugin.paper.lib.parser.v001.GenericParser;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.function.Function;

public final class PaperRustyConnector extends JavaPlugin implements Listener, RustyConnector {
    private Map<String, Config> configs = new HashMap<>();
    private Redis redis;
    private String privateKey;
    private static PaperRustyConnector instance;
    private PluginLogger logger;
    private PaperServer server;
    private PaperCommandManager<CommandSender> manager;
    public boolean hasRegistered = false;

    public MessageCache getMessageCache() {
        return this.redis.getMessageCache();
    }

    public static PaperRustyConnector getInstance() { return PaperRustyConnector.instance; }

    /**
     * Set the Redis handler for the plugin. Once this is set it cannot be changed.
     * @param redis The redis handler to set.
     */
    public void setRedis(Redis redis) throws IllegalStateException {
        if(this.redis != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redis = redis;
    }

    /**
     * Set the PaperServer handler for the plugin. Once this is set it cannot be changed.
     * @param server The PaperServer to set.
     */
    public void setServer(PaperServer server) throws IllegalStateException {
        if(this.server != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.server = server;
    }

    @Override
    public void onEnable() {
        PaperRustyConnector.instance = this;
        this.logger = new PluginLogger(this.getSLF4JLogger());

        if(!loadConfigs()) {
            this.killPlugin();
            return;
        }

        if(!loadCommands()) {
            this.killPlugin();
            return;
        }

        this.logger().log("Started Successfully!");
        this.logger().log("Attempting to register with the proxy...");
        this.server.registerToProxy(this.redis);
        this.hasRegistered = true;
        this.logger().log("Attempt made. Check the console in your proxy to see if it succeeded!");
    }

    @Override
    public void onDisable() {
        if(this.hasRegistered) this.server.unregisterFromProxy(this.redis);

        this.logger().log("Shutting down...");
    }

    @Override
    public InputStream getResourceAsStream(String filename) {
        return this.getResource(filename);
    }

    @Override
    public boolean loadConfigs() {
        try {
            this.logger().log("-| Registering configs");
            this.configs.put("config.yml", new Config(new File(this.getDataFolder(), "config.yml"), "paper_config_template.yml"));
            Config genericConfig = this.configs.get("config.yml");
            if (!genericConfig.register()) return false;

            this.logger().log("---| Preparing proxy...");
            this.logger().log("-----| Registering private key...");
            String privateKey = genericConfig.getData().getNode("private-key").getString();
            if (privateKey == null || privateKey.equals("")) {
                this.logger().log(Lang.border());
                this.logger().log(Lang.spacing());
                this.logger().log("No private-key was defined! You should paste here, the private key that you use on your proxy!");
                this.logger().log(Lang.spacing());
                this.logger().log(Lang.border());
                return false;
            }
            this.logger().log("-----| Finished!");
            this.logger().log("-----| Configuring Server...");

            GenericParser.parse(genericConfig);

            Lang.print(this.logger, Lang.get("wordmark"));

            return true;
        } catch (Exception e) {
            Lang.print(this.logger, Lang.get("boxed-message",e.getMessage()));
        }
        return false;
    }

    @Override
    public boolean loadCommands() {
        try {
            this.manager = new PaperCommandManager<>(
                    /* Owning plugin */ this,
                    CommandExecutionCoordinator.simpleCoordinator(),
                    Function.identity(),
                    Function.identity()
            );

            CommandRusty.create(this.manager);

            return true;
        } catch (Exception e) {
            Lang.print(
                this.logger(),
                Lang.get(
                    "boxed-message",
                    "Commands failed to load! Killing plugin..."
                    )
            );
            this.logger.error("",e);
            return false;
        }
    }

    @Override
    public void reload() {
        this.server = null;
        this.privateKey = null;

        this.loadConfigs();
    }

    public PaperServer getVirtualServer() { return this.server; }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    public boolean validatePrivateKey(String keyToValidate) {
        return this.privateKey.equals(keyToValidate);
    }

    private void killPlugin() {
        this.getPluginLoader().disablePlugin(this);
    }
}
