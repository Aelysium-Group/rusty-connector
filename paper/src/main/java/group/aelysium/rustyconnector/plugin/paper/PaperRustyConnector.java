package group.aelysium.rustyconnector.plugin.paper;

import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.lang_messaging.Lang;
import group.aelysium.rustyconnector.plugin.paper.lib.PaperServer;
import org.bstats.bukkit.Metrics;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public final class PaperRustyConnector extends JavaPlugin implements Listener, RustyConnector {
    private static PaperRustyConnector instance;
    private PluginLogger logger;
    private PaperServer server;
    private PaperCommandManager<CommandSender> commandManager;
    public boolean hasRegistered = false;

    public static PaperRustyConnector getInstance() { return PaperRustyConnector.instance; }

    /**
     * Set the CommandManager for the plugin. Once this is set it cannot be changed.
     * @param commandManager The CommandManager to set.
     */
    public void setCommandManager(PaperCommandManager<CommandSender> commandManager) throws IllegalStateException {
        if(this.commandManager != null) throw new IllegalStateException("This has already been set! You can't set this twice!");
        this.commandManager = commandManager;
    }

    /**
     * Get the command manager for the plugin
     * @return The command manager.
     */
    public PaperCommandManager<CommandSender> getCommandManager() {
        return this.commandManager;
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
        instance = this;
        this.logger = new PluginLogger(this.getSLF4JLogger());

        int pluginId = 17973; // <-- Replace with the id of your plugin!
        Metrics metrics = new Metrics(this, pluginId);

        if(!Engine.start()) this.killPlugin();
    }

    @Override
    public void onDisable() {
        Engine.stop();
    }

    @Override
    public InputStream getResourceAsStream(String filename) {
        return this.getResource(filename);
    }

    @Override
    public void reload() {
        Engine.stop();

        if(!Engine.start()) this.killPlugin();
    }

    public PaperServer getVirtualServer() { return this.server; }

    @Override
    public PluginLogger logger() {
        return this.logger;
    }

    private void killPlugin() {
        this.getPluginLoader().disablePlugin(this);
    }

    public void registerToProxy() {
        this.getVirtualServer().registerToProxy();
    }
}
