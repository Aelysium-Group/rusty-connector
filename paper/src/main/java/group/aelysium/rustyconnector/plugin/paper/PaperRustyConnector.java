package group.aelysium.rustyconnector.plugin.paper;

import cloud.commandframework.paper.PaperCommandManager;
import group.aelysium.rustyconnector.core.RustyConnector;
import group.aelysium.rustyconnector.core.lib.util.logger.Lang;
import group.aelysium.rustyconnector.core.lib.util.logger.LangMessage;
import group.aelysium.rustyconnector.plugin.paper.lib.PaperServer;
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

        if(!Engine.start()) this.killPlugin();

        (new LangMessage(this.logger))
                .insert(Lang.wordmark())
                .print();
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

        (new LangMessage(this.logger))
                .insert(Lang.wordmark())
                .print();
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
