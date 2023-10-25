package group.aelysium.rustyconnector.api.mc_loader.central;

import group.aelysium.rustyconnector.api.core.lang.ILangService;
import group.aelysium.rustyconnector.api.core.lang.ILanguageResolver;
import group.aelysium.rustyconnector.api.core.logger.PluginLogger;
import group.aelysium.rustyconnector.api.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.api.core.messenger.IMessengerConnector;
import net.kyori.adventure.text.Component;

import java.io.InputStream;
import java.util.UUID;

public abstract class MCLoaderTinder {
    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    public static InputStream resourceAsStream(String filename)  {
        return MCLoaderTinder.class.getClassLoader().getResourceAsStream(filename);
    }

    //abstract public S scheduler();

    abstract public void ignite();

    //abstract public MCLoaderFlame<ICoreServiceHandler> flame();

    abstract public PluginLogger logger();

    abstract public ICoreServiceHandler services();

    abstract public String dataFolder();

    abstract public ILangService<? extends ILanguageResolver> lang();

    abstract public void setMaxPlayers(int max);

    abstract public int onlinePlayerCount();

    abstract public UUID getPlayerUUID(String name);

    abstract public String getPlayerName(UUID uuid);

    abstract public boolean isOnline(UUID uuid);

    abstract public void teleportPlayer(UUID uuid, UUID target);

    abstract public void sendMessage(UUID uuid, Component component);
    abstract public MCLoaderFlame<? extends ICoreServiceHandler, ? extends IMessengerConnection<?>, ? extends IMessengerConnector<?>> flame();
}