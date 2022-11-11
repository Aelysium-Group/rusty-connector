package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.core.lib.firewall.MessageTunnel;
import group.aelysium.rustyconnector.core.lib.util.logger.GateKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangKey;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.lib.events.OnPlayerJoin;
import group.aelysium.rustyconnector.plugin.velocity.lib.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.module.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001.GenericParser;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.Callable;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001.LoggerParser;
import org.slf4j.Logger;
import group.aelysium.rustyconnector.core.lib.hash.MD5;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class VelocityRustyConnector implements RustyConnector {
    private static RustyConnector instance;
    private Proxy proxy;
    private Map<String,Config> configs = new HashMap<>();
    private final ProxyServer server;
    private final PluginLogger logger;
    private final File dataFolder;
    private MessageTunnel messageTunnel;

    /**
     * Set the message tunnel for the redis connection. Once this is set it cannot be changed.
     * @param messageTunnel The message tunnel to set.
     */
    public void setMessageTunnel(MessageTunnel messageTunnel) throws IllegalStateException {
        if(this.messageTunnel != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.messageTunnel = messageTunnel;
    }

    public boolean validateMessage(InetSocketAddress address) { return this.messageTunnel.validate(address); }

    public static VelocityRustyConnector getInstance() { return (VelocityRustyConnector) instance; }
    public Proxy getProxy() { return this.proxy; }
    public ProxyServer getVelocityServer() { return this.server; }

    /**
     * Set the Redis handler for the plugin. Once this is set it cannot be changed.
     * @param redis The redis handler to set.
     */
    public void setRedis(Redis redis) throws IllegalStateException {
        if(this.redis != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
    }

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = new PluginLogger(logger);
        this.dataFolder = dataFolder.toFile();
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        this.init();
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {
        this.uninit();
    }

    public void init() {
        instance = this;

        if(!loadConfigs()) return;

        loadCommands();

        registerEvents();
    }

    public void uninit() {
        instance = null;
        this.redis.disconnect();
        this.proxy = null;
        this.messageTunnel = null;
        this.configs = new HashMap<>();
        this.getProxy().killHeartbeat();
        this.getVelocityServer().getCommandManager().unregister("rc");

        this.getVelocityServer().getEventManager().unregisterListener(this, new OnPlayerJoin());
    }

    public void registerEvents() {
        EventManager manager = this.getVelocityServer().getEventManager();

        manager.register(this, new OnPlayerJoin());
    }

    @Override
    public boolean loadConfigs() {
        try {
            this.logger().log("-| Registering configs");
            this.configs.put("config.yml",new Config(new File(this.getDataFolder(), "config.yml"), "velocity_config_template.yml"));
            Config genericConfig = this.configs.get("config.yml");
            this.configs.put("logger.yml",new Config(new File(this.getDataFolder(), "logger.yml"), "velocity_logger_template.yml"));
            Config loggerConfig = this.configs.get("logger.yml");
            if(!genericConfig.register()) return false;

            this.logger().log("---| Preparing proxy...");
            this.logger().log("-----| Registering private key...");
            String privateKey = genericConfig.getData().getNode("private-key").getString();
            if(privateKey == null || privateKey.equals("")) {

                (new LangMessage(this.logger))
                        .insert(Lang.boxedMessage(
                                "No private-key was defined! Generating one now...",
                                "Paste this into the `private-key` field in `config.yml`"
                                ))
                        .insert(Lang.spacing())
                        .insert(MD5.generatePrivateKey())
                        .insert(Lang.spacing())
                        .insert(Lang.border())
                        .print();
                return false;
            }
            this.proxy = new Proxy(privateKey);
            this.logger().log("-----| Finished!");
            this.logger().log("-----| Configuring Proxy...");

            GenericParser.parse(genericConfig);

            LoggerParser.parse(loggerConfig);

            (new LangMessage(this.logger))
                    .insert(Lang.wordmark())
                    .print();

            return true;
        } catch (Exception e) {
            (new LangMessage(this.logger))
                    .insert(Lang.boxedMessage(e.getMessage()))
                    .print();
        }
        return false;
    }

    @Override
    public void reload() {
        this.uninit();
        this.init();
    }

    @Override
    public boolean loadCommands() {
        CommandManager commandManager = server.getCommandManager();

        Callable<Boolean> registerRusty = () -> {
            CommandMeta meta = commandManager.metaBuilder("rustyconnector")
                    .aliases("rusty", "rc")
                    .build();
            BrigadierCommand command = CommandRusty.create();

            commandManager.register(meta, command);

            return true;
        };

        registerRusty.execute();

        return true;
    }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    @Override
    public File getDataFolder() {
        return this.dataFolder;
    }

    @Override
    public InputStream getResourceAsStream(String filename) {
        return getClass().getClassLoader().getResourceAsStream(filename);
    }
}
