package group.aelysium.rustyconnector.toolkit.velocity.central;

import group.aelysium.rustyconnector.toolkit.core.lang.ILangService;
import group.aelysium.rustyconnector.toolkit.core.lang.ILanguageResolver;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnection;
import group.aelysium.rustyconnector.toolkit.core.messenger.IMessengerConnector;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The root api endpoint for the entire RustyConnector api.
 */
public interface VelocityTinder {
    /**
     * Allows access to the {@link PluginLogger} used by RustyConnector.
     * @return {@link PluginLogger}
     */
    PluginLogger logger();

    /**
     * Allows access to RustyConnector's data folder.
     * @return {@link String}
     */
    Path dataFolder();

    ILangService<? extends ILanguageResolver> lang();

    /**
     * Allows access to RustyConnector's available services.
     * @return {@link ICoreServiceHandler}
     * @throws IllegalAccessError If RustyConnector doesn't have any services to give. (Usually because it's still starting, or is being reloaded.)
     */
    ICoreServiceHandler services() throws IllegalAccessError;

    /**
     * Returns the currently active RustyConnector kernel.
     * @return {@link VelocityFlame}
     */
    VelocityFlame flame();

    /**
     * Gets a resource by name and returns it as a stream.
     * @param filename The name of the resource to get.
     * @return The resource as a stream.
     */
    static InputStream resourceAsStream(String filename)  {
        return VelocityTinder.class.getClassLoader().getResourceAsStream(filename);
    }

    /**
     * Schedules a consumer to be executed once a flame has started for RustyConnector.
     * Specifically, this method will run after the base RustyConnector plugin has fully booted.
     * @param callback A consumer. The passed input argument is the newly created Flame instance.
     */
    <TFlame extends VelocityFlame<?>> void onStart(Consumer<TFlame> callback);

    /**
     * Schedules a runnable to be executed once a flame is ready to be killed for RustyConnector.
     * Specifically, this method will run before the base RustyConnector attempts to start shutting down.
     * @param callback A runnable.
     */
    void onStop(Runnable callback);
}
