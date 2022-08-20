package group.aelysium.rustyconnector.plugin.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import group.aelysium.rustyconnector.plugin.velocity.lib.generic.Config;
import ninja.leaping.configurate.ConfigurationNode;
import rustyconnector.RustyConnector;
import rustyconnector.generic.lib.generic.server.Proxy;
import org.slf4j.Logger;
import rustyconnector.generic.lib.hash.MD5;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
        id = "plugin-velocity",
        name = "RustyConnector",
        version = "1.0",
        url = "https://aelysium.group/",
        authors = {"sivin"}
)
public class VelocityRustyConnector implements RustyConnector {
    private Proxy proxy;
    private Map<String,Config> configs = new HashMap<>();
    private final ProxyServer server;
    private final PluginLogger logger;
    private final File dataFolder;

    @Inject
    public VelocityRustyConnector(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = new PluginLogger(logger);
        this.dataFolder = dataFolder.toFile();
        logger.info("Hello there, it's a test plugin I made!");
    }

    @Subscribe
    public void onLoad(ProxyInitializeEvent event) {
        boolean firstStart = !getDataFolder().exists();

        if(!loadConfigs()) return;
    }

    @Subscribe
    public void onUnload(ProxyShutdownEvent event) {

    }

    @Override
    public boolean loadConfigs() {
        this.logger().log("-| Registering configs");
        this.logger().log("---| Registering config.yml");
        this.configs.put("config.yml",new Config(this, new File(this.getDataFolder(), "config.yml"), "config_template.yml"));
        Config genericConfig = this.configs.get("config.yml");
        if(!genericConfig.register()) return false;

        this.logger().log("---| Preparing proxy...");
        this.logger().log("-----| Registering private key...");
        String privateKey = genericConfig.getData().getString("private-key");
        if(privateKey.isEmpty()) {
            this.logger().error("############################################################");
            this.logger().error("No private-key was defined! Generating one now...");
            this.logger().error("Paste this into the private-key field in config.yml");
            this.logger().error(MD5.generatePrivateKey());
            this.logger().error("############################################################");
            return false;
        }
        this.proxy = new Proxy(this, privateKey);
        this.logger().log("-----| Finished!");
        this.logger().log("-----| Configuring Proxy...");
        genericConfig.parse(Config.ParseAs.GENERIC_CONFIG,this.proxy);

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
