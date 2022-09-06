package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.core.lib.generic.firewall.MessageTunnel;
import group.aelysium.rustyconnector.plugin.velocity.commands.CommandRusty;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Proxy;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.database.Redis;
import group.aelysium.rustyconnector.plugin.velocity.lib.parser.v001.GenericParser;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.generic.MessageCache;
import group.aelysium.rustyconnector.core.lib.generic.Callable;
import group.aelysium.rustyconnector.core.lib.generic.Lang;
import group.aelysium.rustyconnector.plugin.velocity.lib.server.PaperServer;
import ninja.leaping.configurate.ConfigurationNode;
import org.slf4j.Logger;
import group.aelysium.rustyconnector.core.lib.generic.hash.MD5;

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
    private Redis redis;
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
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        instance = this;

        if(!loadConfigs()) return;

        loadCommands();

        PaperServer.registerProcessors();
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {

    }

    @Override
    public boolean loadConfigs() {
        try {
            this.logger().log("-| Registering configs");
            this.configs.put("config.yml",new Config(new File(this.getDataFolder(), "config.yml"), "velocity_config_template.yml"));
            Config genericConfig = this.configs.get("config.yml");
            if(!genericConfig.register()) return false;

            this.logger().log("---| Preparing proxy...");
            this.logger().log("-----| Registering private key...");
            String privateKey = genericConfig.getData().getNode("private-key").getString();
            if(privateKey == null || privateKey.equals("")) {

                Lang.print(this.logger, Lang.get(
                        "boxed-message",
                        "No private-key was defined! Generating one now...",
                        "Paste this into the `private-key` field in `config.yml`",
                        Lang.spacing(),
                        Lang.border(),
                        Lang.spacing(),
                        MD5.generatePrivateKey()
                        ));
                return false;
            }
            this.proxy = new Proxy(this, privateKey);
            this.logger().log("-----| Finished!");
            this.logger().log("-----| Configuring Proxy...");

            GenericParser.parse(genericConfig);

            Lang.print(this.logger, Lang.get("wordmark"));

            return true;
        } catch (Exception e) {
            Lang.print(this.logger, Lang.get("boxed-message",e.getMessage()));
        }
        return false;
    }

    @Override
    public void reload() {
        this.redis = null;
        this.proxy = null;
        this.messageTunnel = null;

        this.loadConfigs();
    }

    @Override
    public boolean loadCommands() {
        CommandManager commandManager = server.getCommandManager();

        Callable registerRusty = () -> {
            CommandMeta meta = commandManager.metaBuilder("group/aelysium/rustyconnector/core")
                    .aliases("rusty", "rc")
                    .build();
            BrigadierCommand command = CommandRusty.create();

            commandManager.register(meta, command);
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
