package group.aelysium.rustyconnector.toolkit.premier.central;

import group.aelysium.rustyconnector.toolkit.core.lang.ILangService;
import group.aelysium.rustyconnector.toolkit.core.lang.ILanguageResolver;
import group.aelysium.rustyconnector.toolkit.core.logger.PluginLogger;
import group.aelysium.rustyconnector.toolkit.velocity.central.ICoreServiceHandler;
import group.aelysium.rustyconnector.toolkit.velocity.central.VelocityFlame;

import java.io.InputStream;

/**
 * The root api endpoint for the entire RustyConnector api.
 */
public interface PremierTinder {
    /**
     * Ignites a {@link VelocityFlame} which effectively starts the RustyConnector kernel.
     */
    void ignite() throws RuntimeException;

    /**
     * Allows access to the {@link PluginLogger} used by RustyConnector.
     * @return {@link PluginLogger}
     */
    PluginLogger logger();

    /**
     * Allows access to RustyConnector's data folder.
     * @return {@link String}
     */
    String dataFolder();

    ILangService<? extends ILanguageResolver> lang();

    /**
     * Restarts the entire RustyConnector kernel by exhausting the current {@link VelocityFlame} and igniting a new one.
     */
    void rekindle();

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
        return PremierTinder.class.getClassLoader().getResourceAsStream(filename);
    }
}
