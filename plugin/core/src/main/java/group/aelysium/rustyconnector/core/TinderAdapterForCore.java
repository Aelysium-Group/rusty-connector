package group.aelysium.rustyconnector.core;

import group.aelysium.rustyconnector.api.mc_loader.central.MCLoaderTinder;
import group.aelysium.rustyconnector.api.core.serviceable.ServiceHandler;
import group.aelysium.rustyconnector.core.plugin.lib.lang.PluginLang;

/**
 * Exists as an adapter allowing MCLoader wrappers to use their own implementation of {@link MCLoaderTinder}
 * while still allowing core to have access.
 * <p>
 * If you are writing a new wrapper for MCLoader you MUST initialized this adapter during the boot sequence of your wrapper before you use {@link MCLoaderTinder#ignite()}.
 */
public class TinderAdapterForCore {
    private static MCLoaderTinder tinderInstance;

    public static <S extends ServiceHandler> void init(MCLoaderTinder tinder) {
        tinderInstance = tinder;
    }

    public static MCLoaderTinder getTinder() {
        return tinderInstance;
    }
}