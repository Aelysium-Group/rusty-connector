package group.aelysium.rustyconnector.toolkit.mc_loader.central;

import group.aelysium.rustyconnector.toolkit.core.lang.ILangService;
import group.aelysium.rustyconnector.toolkit.core.lang.ILanguageResolver;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import net.kyori.adventure.text.Component;

import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;

public interface IMCLoaderTinder {
    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    static InputStream resourceAsStream(String filename)  {
        return IMCLoaderTinder.class.getClassLoader().getResourceAsStream(filename);
    }

    PluginLogger logger();

    String dataFolder();

    ILangService<? extends ILanguageResolver> lang();

    void setMaxPlayers(int max);

    int onlinePlayerCount();

    UUID getPlayerUUID(String name);

    String getPlayerName(UUID uuid);

    boolean isOnline(UUID uuid);

    void teleportPlayer(UUID uuid, UUID target);

    void sendMessage(UUID uuid, Component component);

    /**
     * Schedules a consumer to be executed once a flame has started for RustyConnector.
     * Specifically, this method will run after the base RustyConnector plugin has fully booted.
     * @param callback A consumer. The passed input argument is the newly created Flame instance.
     */
    void onStart(Consumer<IMCLoaderFlame<?>> callback);

    /**
     * Schedules a runnable to be executed once a flame is ready to be killed for RustyConnector.
     * Specifically, this method will run before the base RustyConnector attempts to start shutting down.
     * @param callback A runnable.
     */
    void onStop(Runnable callback);
}