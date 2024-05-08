package group.aelysium.rustyconnector.core.mcloader.central;

import group.aelysium.rustyconnector.core.lib.lang.LangService;
import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderFlame;
import group.aelysium.rustyconnector.toolkit.mc_loader.central.IMCLoaderTinder;
import net.kyori.adventure.text.Component;

import java.io.InputStream;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Consumer;

public abstract class MCLoaderTinder implements IMCLoaderTinder {
    protected MCLoaderFlame flame;
    protected final LangService lang;
    protected final PluginLogger pluginLogger;
    protected final Vector<Consumer<? extends IMCLoaderFlame<? extends ICoreServiceHandler>>> onStart = new Vector<>();
    protected final Vector<Runnable> onStop = new Vector<>();

    protected MCLoaderTinder(PluginLogger logger, LangService lang) throws Exception {
        this.pluginLogger = logger;
        this.lang = lang;
    }

    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    public static InputStream resourceAsStream(String filename)  {
        return MCLoaderTinder.class.getClassLoader().getResourceAsStream(filename);
    }

    public MCLoaderFlame flame() {
        return this.flame;
    }

    public PluginLogger logger() {
        return this.pluginLogger;
    }

    public CoreServiceHandler services() {
        return this.flame.services();
    }

    public LangService lang() {
        return this.lang;
    }

    abstract public String dataFolder();

    abstract public void setMaxPlayers(int max);

    abstract public int onlinePlayerCount();

    abstract public UUID getPlayerUUID(String name);

    abstract public String getPlayerName(UUID uuid);

    abstract public boolean isOnline(UUID uuid);

    abstract public void teleportPlayer(UUID uuid, UUID target);

    abstract public void sendMessage(UUID uuid, Component component);

    /**
     * Returns the servers associated with this wrapper.
     * @return An object which can be cast to the appropriate server for this wrapper.
     */
    abstract public Object server();

    /**
     * Ignites a {@link MCLoaderFlame} which effectively starts the RustyConnector kernel.
     * @param port The port that this MCLoader is running on.
     */
    public void ignite(int port) throws RuntimeException {
        this.flame = MCLoaderFlame.fabricateNew(this, this.lang(), this.logger(), port);
    }

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link MCLoaderFlame} and igniting a new one.
     */
    public void rekindle(int port) {
        this.exhaust();

        this.ignite(port);
    }

    /**
     * Kill the {@link MCLoaderFlame}.
     * Typically good for if you want to ignite a new one.
     */
    public void exhaust() {
        this.onStop.forEach(Runnable::run);

        RustyConnector.Toolkit.unregister();

        if(this.flame == null) return;
        this.flame.kill();
        this.flame = null;
    }

    /**
     * Schedules a consumer to be executed once a flame has started for RustyConnector.
     * Specifically, this method will run after the base RustyConnector plugin has fully booted.
     * @param callback A consumer. The passed input argument is the newly created Flame instance.
     */
    public void onStart(Consumer<IMCLoaderFlame<?>> callback) {
        this.onStart.add(callback);
        if(this.flame != null) callback.accept(this.flame);
    }

    /**
     * Schedules a runnable to be executed once a flame is ready to be killed for RustyConnector.
     * Specifically, this method will run before the base RustyConnector attempts to start shutting down.
     * @param callback A runnable.
     */
    public void onStop(Runnable callback) {
        this.onStop.add(callback);
    }
}