package group.aelysium.rustyconnector.plugin.fabric;

import group.aelysium.rustyconnector.toolkit.RustyConnector;
import group.aelysium.rustyconnector.core.TinderAdapterForCore;
import group.aelysium.lib.lang.MCLoaderLang;
import group.aelysium.rustyconnector.plugin.fabric.central.Tinder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;

public final class FabricRustyConnector implements DedicatedServerModInitializer {

    private MinecraftServer server;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;

            Tinder api = Tinder.gather(this, (Logger) LogManager.getLogger());
            TinderAdapterForCore.init(api);

            api.logger().log("Initializing RustyConnector...");
            api.ignite(server.getServerPort());
            MCLoaderLang.WORDMARK_RUSTY_CONNECTOR.send(api.logger(), api.flame().versionAsString());

            RustyConnector.Toolkit.register(api);
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            Tinder.get().exhaust();
        });
    }

    public MinecraftServer getServer() {
        return server;
    }
}