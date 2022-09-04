package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001.GenericParser;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.generic.lib.MessageCache;
import group.aelysium.rustyconnector.core.generic.lib.generic.Callable;
import group.aelysium.rustyconnector.core.generic.lib.generic.Lang;
import org.slf4j.Logger;
import group.aelysium.rustyconnector.core.generic.lib.hash.MD5;
import group.aelysium.rustyconnector.core.generic.lib.hash.Snowflake;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VelocityRustyConnector implements RustyConnector {
    private final Snowflake snowflakeGenerator = new Snowflake();
    private static RustyConnector instance;
    private Proxy proxy;
    private Map<String,Config> configs = new HashMap<>();
    private final ProxyServer server;
    private final PluginLogger logger;
    private final File dataFolder;
    private Redis redis;

    public MessageCache getMessageCache() {
        return this.redis.getMessageCache();
    }

    public static VelocityRustyConnector getInstance() { return (VelocityRustyConnector) instance; }
    public Proxy getProxy() { return this.proxy; }
    public ProxyServer getVelocityServer() { return this.server; }

    /**
     * Set the Redis handler for the plugin. Once this is set it cannot be changed.
     * @param redis The redis handler to set.
     */
    public void setRedis(Redis redis) throws IllegalStateException {
        if(this.redis != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.redis = redis;
    }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = new PluginLogger(logger);
        this.dataFolder = dataFolder.toFile();
        logger.info("Hello there, it's a test plugin I made!");
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        instance = this;

        boolean firstStart = !getDataFolder().exists();

        if(!loadConfigs()) return;

        registerCommands();
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {

    }

    @Override
    public boolean loadConfigs() {
        this.logger().log("-| Registering configs");
        this.configs.put("config.yml",new Config(this, new File(this.getDataFolder(), "config.yml"), "config_template.yml"));
        Config genericConfig = this.configs.get("config.yml");
        if(!genericConfig.register()) return false;

        this.logger().log("---| Preparing proxy...");
        this.logger().log("-----| Registering private key...");
        String privateKey = genericConfig.getData().getNode("private-key").getString();
        if(privateKey == null) {
            this.logger().log(Lang.border());
            this.logger().log("No private-key was defined! Generating one now...");
            this.logger().log("Paste this into the private-key field in config.yml");
            this.logger().log(Lang.border());
            this.logger().log(Lang.spacing());
            this.logger().log(MD5.generatePrivateKey());
            this.logger().log(Lang.spacing());
            this.logger().log(Lang.border());
            return false;
        }
        this.proxy = new Proxy(this, privateKey);
        this.logger().log("-----| Finished!");
        this.logger().log("-----| Configuring Proxy...");

        GenericParser.parse(genericConfig);

        Lang.print(this.logger, Lang.get("wordmark"));

        return true;
    }

    @Override
    public void reload() {
        this.redis = null;
        this.proxy = null;

        this.loadConfigs();
    }

    public void registerCommands() {
        CommandManager commandManager = server.getCommandManager();

        Callable registerRusty = () -> {
            CommandMeta meta = commandManager.metaBuilder("group/aelysium/rustyconnector/core")
                    .aliases("rusty", "rc")
                    .build();
            BrigadierCommand command = CommandRusty.create();

            commandManager.register(meta, command);
        };

        registerRusty.execute();
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public Long newSnowflake() { return this.snowflakeGenerator.nextId(); }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public InputStream getResourceAsStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
